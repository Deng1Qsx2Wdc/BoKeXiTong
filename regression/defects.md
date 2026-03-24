# 缺陷清单

测试日期：2026-03-21

## P0

- `P0` 点赞接口失效。影响页面：`/article/:id`。复现：普通用户调用 `POST /thumbs_up/thumbs` 点赞或取消点赞。预期：返回 `200`，`thumbs_up.status` 与 `article.thumbs_up` 同步切换。实际：稳定返回 `500 系统繁忙，请稍后再试！`，数据库完全不变。
- `P0` 收藏接口失效。影响页面：`/article/:id`、`/profile/favorites`。复现：普通用户调用 `POST /favorite/favorites`。预期：返回 `200`，收藏状态与文章收藏数同步切换。实际：稳定返回 `500`，数据库不变。
- `P0` 关注/取消关注接口失效。影响页面：`/author/:id`、`/profile/follows`。复现：普通用户调用 `POST /follows/follow` 或 `POST /follows/unfollow`。预期：关注关系成功切换。实际：稳定返回 `500`，`follows.status` 不变。
- `P0` 用户资料更新接口失效。影响页面：`/profile/settings`。复现：普通用户调用 `POST /author/update` 修改用户名。预期：返回 `200` 并更新 `author.username`。实际：返回 `500`，数据库无变化。
- `P0` 作者查询接口运行态缺失。影响页面：`/`、`/article/:id`、`/author/:id`、`/profile/follows`。复现：调用 `GET /author/querybyid?id=...`。预期：返回作者资料。实际：运行态返回 `404`。直接后果是首页与详情页作者名退回 `用户{id}`，作者页首屏无法按预期加载。
- `P0` 普通用户可管理分类。影响页面：`/admin/categories` 的服务端权限边界。复现：普通用户 token 调 `POST /category/insert`、`POST /category/update`、补齐 `{id, name}` 后调用 `POST /category/delete`。预期：应被拒绝。实际：均可成功操作数据库。
- `P0` 后台用户列表存在双向权限故障。影响页面：`/admin/users`。复现一：管理员 token 调 `POST /admin/list`。预期：返回用户分页列表。实际：返回 `10001 用户不存在`。复现二：普通用户 token 调同一接口。预期：应被拒绝。实际：返回 `200` 并泄露完整用户列表。

## P1

- `P1` 文章状态切换接口与前端请求体不兼容。影响页面：`/article/edit/:id`、`/profile/articles`。复现：按前端当前请求体 `{id, authorId}` 调 `POST /article/public`、`/article/offline`、`/article/draft`。预期：状态切换成功。实际：被 `Article` 实体的 `title/content/categoryId` 校验拦截，统一返回 `400`。
- `P1` 分类详情接口在游客态被错误鉴权。影响页面：`/article/:id`、`/category/:id`。复现：游客请求 `GET /category/queryone?id=...`。预期：公开返回分类信息。实际：返回 `11002 未提供Token`。这会让游客态详情页的分类标签、分类页标题出现缺失。
- `P1` 后台分类删除按钮必然失败。影响页面：`/admin/categories`。复现：前端当前调用 `categoryDelete(id)`，请求体只有 `{id}`。预期：删除分类成功。实际：后端 `Category` 实体要求 `name` 非空，返回 `400 分类名称不能为空`。
- `P1` 后台用户删除按钮必然失败。影响页面：`/admin/users`。复现：前端当前调用 `authorDelete(id)`，请求体只有 `{id}`。预期：删除用户成功。实际：后端 `Author` 实体要求 `username` 非空，返回 `400 用户名不能为空`。
- `P1` 后台仪表盘分类分布图无法加载。影响页面：`/admin/dashboard`。复现：前端固定调用 `/dashboard/category_article_total?categoryId=0`。预期：返回各分类文章统计。实际：接口返回 `500`，图表区无数据。
- `P1` 后台统计接口公开暴露。影响页面：`/admin/dashboard` 对应服务端。复现：游客直接请求 `GET /dashboard/alltotal`。预期：应校验管理员权限。实际：返回 `200` 和系统统计总数。

## P2

- `P2` 运行服务与工作区源码存在漂移。复现：查看当前源码 `demo-server/src/main/java/com/example/demo/config/WebMvcConfig.java`，`/author/query`、`/author/queryone`、`/author/querybyid`、`/category/queryone` 均被配置为公开；但运行态中这些接口仍要求 Token 或直接 `404`。影响：会让前端排查结果与本地源码阅读结果不一致。
- `P2` 首页、关注页、详情页存在用户名回退逻辑，掩盖了根因。复现：查看 `frontend/src/pages/home/Home.tsx`、`frontend/src/pages/article/ArticleDetail.tsx`、`frontend/src/pages/user/UserFollows.tsx`。预期：作者名应始终由 `username` 提供。实际：当 `/author/querybyid` 失败时，前端退回 `用户{id}`，表面症状像“展示错了”，但根因其实是作者查询接口不可用。
- `P2` 通知铃铛尚未完成端到端验证。影响页面：全局 `Header`。当前源码为前端内存态通知列表叠加 `useWebSocket` 连接，但本轮未完成真实浏览器 + WebSocket 联调，因此“角标、已读、清空、跨用户推送”仍需补测。

## 建议回归顺序

- 先修复接口与权限：`/author/querybyid`、`/thumbs_up/thumbs`、`/favorite/favorites`、`/follows/follow`、`/follows/unfollow`、`/author/update`、`/admin/list`、`/category/**` 权限边界。
- 再修复前后端契约不一致：`/article/public`、`/article/offline`、`/article/draft`、`/category/delete`、`/author/delete`。
- 最后补做真实浏览器回归：首页分类切换、编辑器分类下拉、Header/菜单、通知铃铛、路由守卫、管理员页面交互。


