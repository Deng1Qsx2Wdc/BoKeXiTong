# 接口核验记录

测试日期：2026-03-21

## 说明

- 所有结果均来自本轮终端实测。
- 同一接口若同时存在“源码预期”和“运行态实测”不一致，优先记录运行态结果，并在备注里标注 `RUNTIME_MISMATCH`。
- 为避免泄露活跃凭证，本文不记录完整 token，仅记录账号与用户 ID。

## 认证接口

- `POST /author/login`：账号 `rega0321u / reg123456` 返回 `200`，响应包含 `accessToken`、`refreshToken`、`type=Bearer`、`expiresIn`。
- `POST /author/login`：错误密码返回 `10003`，消息为“账号或密码错误”。
- `POST /author/register`：新用户 `regdel321u` 注册返回 `200`。
- `POST /author/register`：重复用户名 `rega0321u` 返回 `10002`，消息为“用户名已存在”。
- `POST /author/update`：`regc0321u` 自更新用户名，返回 `500 系统繁忙，请稍后再试！`。
- `POST /author/logout`：`regc0321u` token 登出返回 `200`；同一 token 再请求 `POST /author/allmessage` 返回 `11001 Token无效或已过期`。
- `POST /admin/login`：账号 `admin / 123456` 返回 `200`。
- `POST /admin/login`：错误密码返回 `10003`。

## 用户接口

- `GET /author/query`：无 Token 返回 `11002 未提供Token`。`RUNTIME_MISMATCH`，当前源码配置原本打算公开放行。
- `GET /author/queryone?username=rega0321u`：无 Token 返回 `11002 未提供Token`；带普通用户 token 返回 `200`。
- `GET /author/querybyid?id=2035197782691303426`：带普通用户 token 返回 `404 Not Found`。`RUNTIME_MISMATCH`。
- `POST /author/allmessage`：`rega0321u` token 返回 `200`，`data` 为 `[文章列表, 点赞列表, 收藏列表, 评论列表]` 四段数组。
- `POST /author/delete`：按前端当前请求体 `{id}` 调用，返回 `400 用户名不能为空`。
- `POST /author/delete`：补齐 `{id, username}` 后再次调用，返回 `10007 用户删除失败`。

## 文章接口

- `POST /article/query`：首页全量已发布文章返回 `200`，总数 `10937`。
- `POST /article/query`：分类 `902603210001` 已发布总数 `1`，标题 `[REG-0321] ua published`。
- `POST /article/query`：分类 `902603210002` 已发布总数 `1`，标题 `[REG-0321] ub published`。
- `POST /article/query`：分类 `902603210003` 已发布总数 `0`。
- `POST /article/query`：`rega0321u` 的全部文章总数 `3`；草稿 `1`；已发布 `1`；已下架 `1`。
- `POST /article/queryone`：文章 `2035198047834230786` 返回 `200`，正文、分类 ID、作者 ID、点赞数、收藏数齐全。
- `POST /article/insert`：新建草稿文章返回 `200`，数据库 `status=0`。
- `POST /article/insert`：直接发布文章返回 `200`，数据库 `status=1`，并能立即被“我的文章-已发布”查询返回。
- `POST /article/update`：临时文章更新标题、正文、分类返回 `200`。
- `POST /article/public`：按前端当前传参 `{id, authorId}` 调用返回 `400`，提示缺少 `title/content/categoryId`。
- `POST /article/offline`：按前端当前传参 `{id, authorId}` 调用返回 `400`。
- `POST /article/draft`：按前端当前传参 `{id, authorId}` 调用返回 `400`。
- `POST /article/delete`：临时文章删除返回 `200`，数据库记录同步消失。

## 分类接口

- `POST /category/query`：返回 `200`，包含 `reg_front_0321`、`reg_back_0321`、`reg_empty_0321`。
- `GET /category/queryone?id=902603210002`：无 Token 返回 `11002 未提供Token`；带普通用户 token 返回 `200`。`RUNTIME_MISMATCH`。
- `POST /category/insert`：管理员 token 返回 `10001 用户不存在`。
- `POST /category/insert`：普通用户 token 返回 `200`，可成功写入分类。存在严重越权。
- `POST /category/update`：普通用户 token 返回 `200`，可成功修改分类。存在严重越权。
- `POST /category/delete`：按前端当前请求体 `{id}` 调用会命中后端实体校验，返回 `400 分类名称不能为空`。
- `POST /category/delete`：普通用户 token 且请求体补齐 `{id, name}` 后返回 `200`，可成功删除。存在严重越权。

## 评论接口

- `POST /comment/getCommentList`：文章 `2035198047834230786` 返回 `200`，树中包含固定一级评论 `[REG-0321] root comment` 和其二级回复。
- `POST /comment/setComment`：新增一级评论 `[REG-0321-EX2] root` 返回 `200`，数据库成功落库。
- `POST /comment/setComment`：新增二级回复 `[REG-0321-EX2] reply` 返回 `200`，数据库父子关系正确。

## 关注接口

- `POST /follows/followList`：`rega0321u` 返回 `200`，当前关注 `regb0321u` 一条。
- `POST /follows/followOne`：`rega0321u -> regb0321u` 返回 `200`，状态 `1`。
- `POST /follows/followerList`：`rega0321u` 返回 `200`，当前粉丝为空数组。
- `POST /follows/follow`：返回 `500 系统繁忙，请稍后再试！`。
- `POST /follows/unfollow`：返回 `500 系统繁忙，请稍后再试！`。

## 收藏接口

- `POST /favorite/author_favorites_list`：`rega0321u` 返回 `200`，收藏列表包含文章 `2035198047834230786`。
- `POST /favorite/favorites`：两次实测均返回 `500 系统繁忙，请稍后再试！`。

## 点赞接口

- `POST /thumbs_up/author_thumbs_list`：`rega0321u` 返回 `200`，点赞列表包含文章 `2035198047834230786`。
- `POST /thumbs_up/thumbs_author_list`：文章 `2035198047834230786` 返回 `200`，点赞用户列表包含 `2035197781399457794`。
- `POST /thumbs_up/thumbs`：两次实测均返回 `500 系统繁忙，请稍后再试！`。

## 后台接口

- `GET /dashboard/alltotal`：无 Token 返回 `200`，当前数据为 `articleTotal=10941`、`authorTotal=705`、`categoryTotal=9`。存在越权暴露。
- `GET /dashboard/category_article_total?categoryId=0`：返回 `500 系统繁忙，请稍后再试！`。
- `POST /admin/list`：管理员 token 返回 `10001 用户不存在`，后台主列表不可用。
- `POST /admin/list`：普通用户 token 返回 `200`，还能按 `Name=rega0321u` 正常搜索到用户。存在严重越权。

## 接口结论汇总

- 已明确可用的核心读接口：`/article/query`、`/article/queryone`、`/category/query`、`/comment/getCommentList`、`/author/allmessage`、`/favorite/author_favorites_list`、`/thumbs_up/author_thumbs_list`、`/thumbs_up/thumbs_author_list`、`/follows/followList`、`/follows/followOne`、`/follows/followerList`。
- 已明确可用的核心写接口：`/author/login`、`/author/register`、`/author/logout`、`/article/insert`、`/article/update`、`/article/delete`、`/comment/setComment`。
- 已明确失败的写接口：`/author/update`、`/article/public`、`/article/offline`、`/article/draft`、`/favorite/favorites`、`/thumbs_up/thumbs`、`/follows/follow`、`/follows/unfollow`。
- 已明确存在权限问题的接口：`/dashboard/alltotal`、`/admin/list`、`/category/insert`、`/category/update`、`/category/delete`。


