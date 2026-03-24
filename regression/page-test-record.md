# 全页面全功能回归测试记录

测试日期：2026-03-21

## 执行说明

- 本轮目标是按计划覆盖当前已挂载的 17 个前端路由页面，以及 `Header`、搜索框、通知铃铛、`AuthGuard`、`AdminGuard`。
- 当前终端环境无法稳定拉起可交互浏览器做真实点击式手工操作。此前已尝试 Headless Edge/CDP，但运行不稳定，无法形成可信的逐像素 UI 结论。
- 因此本轮记录采用三类证据合并输出：
- `PASS`：接口与数据库已直接验证通过。
- `FAIL`：接口或数据库已直接验证失败。
- `BLOCKED`：需要真实浏览器点击/视觉确认，当前环境未完成。
- `RUNTIME_MISMATCH`：运行中的 `8080` 服务与当前工作区源码暴露出的行为不一致。

## 测试基线

- 前端工作区：`frontend`
- 后端服务：`http://localhost:8080`
- 数据库：`blog_system`
- 固定用户：
- `rega0321u`，ID `2035197781399457794`
- `regb0321u`，ID `2035197782691303426`
- `regc0321u`，ID `2035197783660187649`
- 固定分类：
- `reg_front_0321`，ID `902603210001`
- `reg_back_0321`，ID `902603210002`
- `reg_empty_0321`，ID `902603210003`
- 固定文章：
- `2035198046215229441`，`[REG-0321] ua draft`，草稿
- `2035198046861152257`，`[REG-0321] ua published`，已发布
- `2035198047377051649`，`[REG-0321] ua offline`，已下架
- `2035198047834230786`，`[REG-0321] ub published`，已发布

## 六类历史问题回归结论

- 首页分类切换只有“全部”有数据：`PASS`。`/article/query` 已验证分类筛选可返回前端分类 1 篇、后端分类 1 篇、空分类 0 篇；`BLOCKED` 于真实浏览器标签点击观感。
- 新文章分类下拉选不中：`BLOCKED`。源码中 `select` 已绑定 `value={categoryId}` 与 `onChange`，但未在真实浏览器完成点击验证。
- 草稿文章不出现在“我的文章-草稿”：`PASS`。`authorId + status=0` 查询返回 `[REG-0321] ua draft`。
- 发布文章不出现在“我的文章-已发布”：`PASS`。直接以 `status=1` 新建临时文章后，`authorId + status=1` 查询立即可见。
- 点赞报“系统繁忙，请稍后再试！”：`FAIL`。`POST /thumbs_up/thumbs` 两次实测均返回 `500`，数据库点赞状态与文章点赞计数均未变化。
- 用户名显示为 ID 而不是 username：`FAIL`。`/author/querybyid` 运行态返回 `404`，首页、文章详情、作者页、关注页均依赖该接口，前端会退回 `用户{id}` 或直接拿不到作者信息。

## 游客页面

- `/` 首页：`PASS` 文章列表、搜索参数、分类筛选的后端查询链路；`FAIL` 作者名回填依赖 `/author/querybyid`，当前运行态 `404`，会退回 `用户{id}`；`BLOCKED` 分类标签点击样式、分页点击、卡片跳转、搜索框展开收起的真实浏览器操作。
- `/login` 登录页：`PASS` 用户登录、管理员登录、错误密码提示的 API；`BLOCKED` 空表单校验、从受保护页返回原页面的真实页面跳转。
- `/register` 注册页：`PASS` 新用户注册、重复用户名拦截；`BLOCKED` 输入框级文案、焦点、错误提示样式。
- `/article/:id` 文章详情：`PASS` 文章详情查询与评论树查询；`FAIL` 游客态分类信息依赖 `/category/queryone`，当前运行态无 Token 时返回 `11002`，游客无法稳定看到分类面包屑；`FAIL` 作者名依赖 `/author/querybyid`，当前运行态 `404`；`BLOCKED` 游客点击点赞/收藏/评论的界面拦截提示样式。
- `/category/:id` 分类页：`PASS` 分类文章列表查询；`FAIL` 分类标题依赖 `/category/queryone`，当前运行态无 Token 时返回 `11002`；`BLOCKED` 分页点击与空态样式。
- `/author/:id` 作者页：`FAIL` 页面首屏依赖 `/author/querybyid`，当前运行态 `404`，作者页无法按预期拿到作者资料；`BLOCKED` 未登录不显示关注按钮的真实渲染。
- `*` 404：`BLOCKED`。源码中已挂载 `NotFound` 路由并提供返回首页按钮，但未在真实浏览器访问未知地址。

## 普通用户页面

- `/article/new` 写新文章：`PASS` 保存草稿走 `POST /article/insert` 且 `status=0`；`PASS` 立即发布走 `POST /article/insert` 且 `status=1`，随后能在“我的文章-已发布”查询到；`BLOCKED` 分类下拉真实点击选中、表单错误提示样式与提交后页面跳转。
- `/article/edit/:id` 编辑文章：`PASS` 文章旧数据回填依赖的 `POST /article/queryone` 与 `POST /article/update`；`FAIL` “改为草稿”“发布”“下架”三类状态切换都调用了被 `@Valid` 拦截的接口，返回 `400`；`BLOCKED` 已选分类在浏览器中的默认选中态。
- `/profile` 个人中心：`PASS` `POST /author/allmessage` 可正确返回文章、点赞、收藏、评论四段数据；`BLOCKED` 头像首字母、角色文案、导航入口点击。
- `/profile/articles` 我的文章：`PASS` “全部/草稿/已发布/已下架”四种查询均能正确返回对应文章；`FAIL` 页面内“发布/下架/存草稿”按钮会命中状态接口 `400`；`PASS` 删除文章 API 与数据库清理正确；`BLOCKED` 筛选按钮激活态与删除确认弹窗。
- `/profile/favorites` 我的收藏：`PASS` 收藏列表读取正常，能返回文章 ID 并二次拉取文章详情；`FAIL` 收藏写操作本身返回 `500`，因此“新增收藏/取消收藏”来源页不可用；`BLOCKED` 空态和详情跳转的真实点击。
- `/profile/follows` 我的关注：`PASS` 关注列表、粉丝列表、关注关系查询正常；`FAIL` 用户名回填依赖 `/author/querybyid`，当前运行态 `404`，前端会退回 `用户{id}`；`FAIL` 取消关注接口返回 `500`；`BLOCKED` 页签切换与作者页跳转。
- `/profile/settings` 账号设置：`FAIL` `POST /author/update` 返回 `500`，用户名修改无法完成；`PASS` `POST /author/logout` 正常，登出后旧 token 再访问受保护接口返回 `11001`；`BLOCKED` 前端密码一致性校验与退出后页面跳转。
- 文章详情页登录态：`FAIL` 点赞与收藏写接口均返回 `500`；`PASS` 新增一级评论与二级回复均成功落库；`PASS` 删除文章接口可用；`BLOCKED` 本人文章编辑/删除按钮显隐与弹窗交互。
- 作者页登录态：`FAIL` 页面仍受 `/author/querybyid` 运行态 `404` 影响；`FAIL` 关注与取消关注均返回 `500`；`BLOCKED` 自访问本人作者页不显示关注按钮的真实渲染。

## 管理员页面

- `/admin/login` 管理员登录：`PASS` 正确账号登录、错误密码拦截；`BLOCKED` 空表单校验与页面提示样式。
- `/admin/dashboard` 仪表盘：`PASS` 统计卡片依赖的 `/dashboard/alltotal` 可返回数据；`FAIL` 分类分布接口 `/dashboard/category_article_total?categoryId=0` 返回 `500`，图表区无法按当前实现加载；`FAIL` 统计接口无 Token 也可直接访问，存在越权暴露；`BLOCKED` 页面内跳转按钮点击。
- `/admin/users` 用户管理：`FAIL` 管理员 token 调 `/admin/list` 返回 `10001 用户不存在`，页面主列表不可用；`FAIL` 普通用户 token 反而能读取 `/admin/list`，存在严重权限漏洞；`FAIL` 删除用户按钮按当前前端请求体只传 `id`，会被后端 `username` 非空校验拦截；`BLOCKED` 分页点击与空结果样式。
- `/admin/categories` 分类管理：`PASS` 分类列表查询正常；`FAIL` 管理员 token 对新增分类返回 `10001 用户不存在`；`FAIL` 普通用户 token 却可新增、修改、删除分类，存在严重权限漏洞；`FAIL` 页面删除按钮当前只传 `id`，后端要求 `name`，会返回 `400 分类名称不能为空`；`BLOCKED` 弹窗开关与输入框交互。

## 跨页与全局功能

- `Header`：`BLOCKED`。源码显示游客态渲染“登录/注册”，普通用户渲染“写新文章/个人菜单”，管理员渲染“管理后台/个人菜单”，但未完成真实浏览器核验。
- 用户菜单：`BLOCKED`。源码已实现展开/收起、点击外部关闭、跳转与退出；未完成真实浏览器核验。
- 搜索框：`PASS` 首页支持 `keyword` 参数查询；`BLOCKED` 搜索框展开、输入、提交后的真实浏览器动作。
- 通知铃铛：`BLOCKED`。源码为前端内存态列表加 WebSocket 推送，未做真实链路验证；当前无法确认角标、已读、清空在浏览器中的实际表现。
- WebSocket 通知链路：`BLOCKED`。当前终端环境未完成真实浏览器 + WebSocket 端到端联调。
- `AuthGuard`：`BLOCKED`。源码确认未登录会重定向 `/login` 且保留 `from`；未做真实页面跳转验证。
- `AdminGuard`：`BLOCKED`。源码确认未登录跳 `/admin/login`，普通用户跳首页；未做真实页面跳转验证。
- 登录态持久化：`BLOCKED`。源码基于 `localStorage` 读写 token 与 userInfo；未在真实浏览器执行刷新保活。

## 运行态不一致说明

- 当前 `8080` 运行服务与工作区源码不完全一致，至少体现在 `/author/query`、`/author/queryone`、`/author/querybyid`、`/category/queryone` 的鉴权与可用性上。
- 同一会话内，`curl` 与 `Invoke-RestMethod` 在少数接口上也暴露了不同表现；文档里只采信重复验证后较稳定的结论。
- 因此本轮报告已尽量把“接口真实结果”和“源码预期结果”分开记录，避免把运行态漂移误写成前端单点问题。


