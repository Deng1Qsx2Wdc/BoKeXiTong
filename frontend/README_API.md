# 前端 API 使用文档

## 目录结构

```
src/
├── types/
│   └── index.ts          # TypeScript 类型定义
└── api/
    ├── client.ts         # Axios 客户端配置（拦截器）
    ├── index.ts          # API 统一导出
    ├── admin.ts          # 管理员模块 API
    ├── author.ts         # 用户模块 API
    ├── article.ts        # 文章模块 API
    ├── category.ts       # 分类模块 API
    ├── comment.ts        # 评论模块 API
    ├── thumbsUp.ts       # 点赞模块 API
    ├── favorite.ts       # 收藏模块 API
    ├── follows.ts        # 关注模块 API
    └── dashboard.ts      # 仪表盘模块 API
```

## 配置

### 环境变量

在项目根目录创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 使用示例

### 1. 用户登录

```typescript
import { authorLogin } from '@/api';

const handleLogin = async () => {
  try {
    const response = await authorLogin({
      name: 'username',
      password: 'password123'
    });

    const { token, author } = response.data.data!;

    // 保存 token（拦截器会自动使用）
    localStorage.setItem('token', token);

    console.log('登录成功:', author);
  } catch (error) {
    console.error('登录失败:', error);
  }
};
```

### 2. 查询文章列表

```typescript
import { articleQuery } from '@/api';

const fetchArticles = async () => {
  try {
    const response = await articleQuery({
      pageNum: 1,
      pageSize: 10,
      categoryId: 1,
      status: 1 // 已发布
    });

    const { records, total, pages } = response.data.data!;
    console.log('文章列表:', records);
    console.log('总数:', total);
  } catch (error) {
    console.error('查询失败:', error);
  }
};
```

### 3. 发布文章

```typescript
import { articleInsert } from '@/api';

const publishArticle = async () => {
  try {
    await articleInsert({
      categoryId: 1,
      title: '文章标题',
      content: '文章内容...',
      status: 1 // 1-已发布, 0-草稿
    });

    console.log('发布成功');
  } catch (error) {
    console.error('发布失败:', error);
  }
};
```

### 4. 点赞文章

```typescript
import { thumbsUpToggle } from '@/api';

const handleLike = async (articleId: number) => {
  try {
    await thumbsUpToggle(articleId);
    console.log('点赞操作成功');
  } catch (error) {
    console.error('点赞失败:', error);
  }
};
```

### 5. 添加评论

```typescript
import { commentSet } from '@/api';

const addComment = async (articleId: number, content: string) => {
  try {
    await commentSet({
      articleId,
      content,
      parentId: undefined // 一级评论，回复评论时传入父评论ID
    });

    console.log('评论成功');
  } catch (error) {
    console.error('评论失败:', error);
  }
};
```

### 6. 关注用户

```typescript
import { followsFollow, followsUnfollow } from '@/api';

const handleFollow = async (targetId: number, authorId: number) => {
  try {
    await followsFollow({ authorId, targetId });
    console.log('关注成功');
  } catch (error) {
    console.error('关注失败:', error);
  }
};

const handleUnfollow = async (targetId: number, authorId: number) => {
  try {
    await followsUnfollow({ authorId, targetId });
    console.log('取消关注成功');
  } catch (error) {
    console.error('取消关注失败:', error);
  }
};
```

## API 模块说明

### 管理员模块 (`admin.ts`)

- `adminLogin(data)` - 管理员登录
- `adminListUsers(pageNum, pageSize, Name)` - 查询用户列表

### 用户模块 (`author.ts`)

- `authorLogin(data)` - 用户登录
- `authorRegister(data)` - 用户注册
- `authorDelete(id)` - 删除用户
- `authorQuery(Name, PageNum, PageSize)` - 查询用户列表
- `authorQueryOne(username)` - 查询单个用户
- `authorUpdate(data)` - 更新用户信息
- `authorAllMessage()` - 获取用户所有信息

### 文章模块 (`article.ts`)

- `articleInsert(data)` - 添加文章
- `articleDelete(data)` - 删除文章
- `articleQuery(data)` - 查询文章列表
- `articleQueryOne(id)` - 查询单篇文章
- `articleUpdate(data)` - 更新文章
- `articleDraft(data)` - 改为草稿
- `articlePublic(data)` - 发布文章
- `articleOffline(data)` - 下架文章

### 分类模块 (`category.ts`)

- `categoryInsert(data)` - 添加分类
- `categoryDelete(id)` - 删除分类
- `categoryQuery(data)` - 查询分类列表
- `categoryQueryOne(id)` - 查询单个分类
- `categoryUpdate(data)` - 更新分类

### 评论模块 (`comment.ts`)

- `commentSet(data)` - 添加评论
- `commentGetList(articleId)` - 获取文章评论列表（树形结构）

### 点赞模块 (`thumbsUp.ts`)

- `thumbsUpToggle(id)` - 点赞/取消点赞文章
- `thumbsUpAuthorList(id)` - 获取点赞某篇文章的用户列表
- `thumbsUpMyList()` - 获取当前用户的点赞列表

### 收藏模块 (`favorite.ts`)

- `favoriteToggle(id)` - 收藏/取消收藏文章
- `favoriteMyList()` - 获取当前用户的收藏列表

### 关注模块 (`follows.ts`)

- `followsGetFollowList(id)` - 获取用户的关注列表
- `followsCheckOne(params)` - 查询是否关注某个用户
- `followsGetFollowerList(id)` - 获取用户的粉丝列表
- `followsFollow(params)` - 关注用户
- `followsUnfollow(params)` - 取消关注
- `followsGetArticles(Page, PageSize)` - 获取关注用户的文章列表

### 仪表盘模块 (`dashboard.ts`)

- `dashboardGetAllTotal()` - 获取系统统计数据
- `dashboardGetCategoryArticleTotal(categoryId)` - 获取各分类的文章统计

## 拦截器功能

### 请求拦截器

- 自动从 `localStorage` 读取 token
- 自动添加 `Authorization: Bearer <token>` 请求头

### 响应拦截器

- 统一处理业务状态码（code: 200 表示成功）
- 自动处理 HTTP 错误状态码：
  - `401` - 清除 token，提示重新登录
  - `403` - 权限不足
  - `404` - 资源不存在
  - `500` - 服务器错误

## 类型定义

所有类型定义在 `src/types/index.ts` 中，包括：

- `Result<T>` - 通用响应结构
- `Page<T>` - 分页响应结构
- `Admin` - 管理员实体
- `Author` - 用户实体
- `Article` - 文章实体
- `Category` - 分类实体
- `Comment` - 评论实体
- `Thumbs_up` - 点赞实体
- `Favorites` - 收藏实体
- `Follows` - 关注实体
- `Dashboard` - 仪表盘统计数据
- 以及各种请求参数类型

## 注意事项

1. **Token 管理**：登录成功后需要手动保存 token 到 `localStorage`，拦截器会自动使用
2. **错误处理**：所有 API 调用都应该使用 try-catch 处理错误
3. **类型安全**：所有 API 函数都有完整的 TypeScript 类型定义
4. **字段命名**：类型定义中的字段名与后端 JSON 严格一致
5. **日期格式**：后端返回的日期字段为字符串类型，需要前端自行转换
