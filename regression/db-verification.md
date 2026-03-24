# 数据库核验记录

测试日期：2026-03-21

## 核验范围

- 目标库：`blog_system`
- 核验表：
- `author`
- `admin`
- `article`
- `category`
- `comment`
- `follows`
- `favorites`
- `thumbs_up`

## 固定基线数据

### `author`

| username | id |
| --- | --- |
| rega0321u | 2035197781399457794 |
| regb0321u | 2035197782691303426 |
| regc0321u | 2035197783660187649 |

### `category`

| name | id |
| --- | --- |
| reg_front_0321 | 902603210001 |
| reg_back_0321 | 902603210002 |
| reg_empty_0321 | 902603210003 |

### `article`

| id | title | author_id | status |
| --- | --- | --- | --- |
| 2035198046215229441 | [REG-0321] ua draft | 2035197781399457794 | 0 |
| 2035198046861152257 | [REG-0321] ua published | 2035197781399457794 | 1 |
| 2035198047377051649 | [REG-0321] ua offline | 2035197781399457794 | 2 |
| 2035198047834230786 | [REG-0321] ub published | 2035197782691303426 | 1 |

### 互动基线

| table | key | 当前值 |
| --- | --- | --- |
| follows | `rega0321u -> regb0321u` | `status=1` |
| favorites | `rega0321u -> 2035198047834230786` | `status=1` |
| thumbs_up | `rega0321u -> 2035198047834230786` | `status=1` |
| article | `2035198047834230786` | `thumbs_up=1, favorites=1` |

## 数据落库核验

- 注册新用户 `regdel321u` 后，`author` 表新增了 `id=2035201544537395202` 的记录。该记录已在验证结束后通过 SQL 清理。
- `POST /article/insert` 新建草稿临时文章后，`article` 表出现 `id=2035201818148622338`，`status=0`，`author_id=2035197781399457794`。验证结束后已删除。
- `POST /article/insert` 直接发布临时文章后，`article` 表出现 `id=2035202438838505474`，`status=1`。验证结束后已删除。
- `POST /article/update` 更新临时文章后，`article` 表中 `id=2035202690953924611` 的 `title`、`content`、`category_id` 均按请求修改成功；验证结束后已删除。
- `POST /article/delete` 删除临时文章后，对应 `article` 记录查询结果为 `0`，数据库与接口结果一致。
- `POST /article/public`、`/article/offline`、`/article/draft` 失败后，临时文章 `2035201818148622338` 的 `status` 保持 `0`，数据库未发生错误切换。

## 互动数据核验

- 点赞接口 `POST /thumbs_up/thumbs` 两次返回 `500` 后，`thumbs_up` 表中 `rega0321u -> 2035198047834230786` 的 `status` 仍为 `1`，`article.thumbs_up` 仍为 `1`。
- 收藏接口 `POST /favorite/favorites` 两次返回 `500` 后，`favorites` 表中 `rega0321u -> 2035198047834230786` 的 `status` 仍为 `1`，`article.favorites` 仍为 `1`。
- 关注接口 `POST /follows/unfollow` 与 `POST /follows/follow` 返回 `500` 后，`follows` 表中 `rega0321u -> regb0321u` 的 `status` 仍为 `1`。
- 评论接口 `POST /comment/setComment` 新增一级评论 `[REG-0321-EX2] root` 后，`comment` 表新增 `id=2035200879341752321`，`parent_id=NULL`。
- 评论接口继续新增二级回复 `[REG-0321-EX2] reply` 后，`comment` 表新增 `id=2035200927630774273`，`parent_id=2035200879341752321`。

## 分类数据核验

- 普通用户 token 调 `POST /category/insert` 后，`category` 表成功插入临时分类 `reg_user_probe_0321`，验证后已删除。
- 普通用户 token 调 `POST /category/update` 后，临时分类 `2035201144040083457` 的 `name` 成功从 `reg_user_probe_0321_u1` 改为 `reg_user_probe_0321_u2`；该记录随后已清理。
- 普通用户 token 且补齐 `{id, name}` 调 `POST /category/delete` 后，临时分类 `reg_user_probe_0321_del` 在 `category` 表中的记录数变为 `0`。
- 按前端当前删除参数只传 `id` 时，接口虽然返回 `400`，数据库记录仍保持存在，未出现误删。

## 用户数据核验

- `POST /author/update` 返回 `500` 后，`regc0321u` 在 `author` 表中的 `username` 未发生变化。
- `POST /author/delete` 按前端当前请求体只传 `id` 时失败，临时用户 `regdel321u` 仍保留在 `author` 表中。
- `POST /author/delete` 补齐 `{id, username}` 后仍返回 `10007`，临时用户记录仍存在；该记录随后已通过 SQL 手工清理。

## 数据库结论

- 已验证“读链路正常、写链路失败”的数据特征非常明显：多条查询接口表现正常，但点赞、收藏、关注、账户更新、文章状态切换等写操作没有成功改动库表。
- 已验证多处“接口与前端请求体不匹配”的问题：例如分类删除、用户删除。
- 已验证至少一组严重越权：普通用户可以对 `category` 表完成新增、修改、删除。


