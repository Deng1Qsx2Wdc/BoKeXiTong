/**
 * 通用响应结构
 */
export interface Result<T = unknown> {
  code: number;
  msg?: string;
  data?: T;
}

export type Id = string;

/**
 * 分页响应结构
 */
export interface Page<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 管理员实体
 */
export interface Admin {
  id: Id;
  username: string;
  password: string;
  token?: string;
}

/**
 * 用户实体
 */
export interface Author {
  id: Id;
  username: string;
  password: string;
  token?: string;
}

/**
 * 文章实体
 */
export interface Article {
  id: Id;
  categoryId: Id;
  title: string;
  content: string;
  authorId: Id;
  createTime: string;
  updateTime: string;
  status: number; // 0-草稿 1-已发布 2-已下线
  thumbsUp: number;
  favorites: number;
}

/**
 * 分类实体
 */
export interface Category {
  id: Id;
  name: string;
}

/**
 * 评论实体
 */
export interface Comment {
  id: Id;
  authorId: Id;
  articleId: Id;
  parentId: Id | null;
  createTime: string;
  content: string;
  children?: Comment[];
}

/**
 * 点赞实体
 */
export interface Thumbs_up {
  id: Id;
  authorId: Id;
  articleId: Id;
  thumbsUpTime: string;
  status: number; // 0-取消点赞 1-已点赞
}

/**
 * 收藏实体
 */
export interface Favorites {
  id: Id;
  authorId: Id;
  articleId: Id;
  favoritesTime: string;
  status: number; // 0-取消收藏 1-已收藏
}

/**
 * 关注实体
 */
export interface Follows {
  id: Id;
  authorId: Id;
  targetId: Id;
  followTime: string;
}

/**
 * 仪表盘统计数据
 */
export interface Dashboard {
  articleTotal: number;
  authorTotal: number;
  categoryTotal: number;
  latest_seven_days_article_total: number;
}

/**
 * 分类文章统计
 */
export interface Cate_Ari_Total {
  id: Id;
  name: string;
  count: number;
}

/**
 * 登录请求参数
 */
export interface Login {
  name: string;
  password: string;
}

/**
 * 登录响应数据（管理员）
 */
export interface AdminLoginResponse {
  token: string;
  admin: Admin;
}

/**
 * 登录响应数据（用户）
 */
export interface AuthorLoginResponse {
  accessToken: string;
  refreshToken: string;
  Type: string;
  expiresIn: number;
}

/**
 * 文章查询参数
 */
export interface ArticleQuery {
  pageSize: number;
  pageNum: number;
  authorId?: Id;
  keyword?: string;
  categoryId?: Id;
  status?: number;
  startTime?: string;
  endTime?: string;
}

/**
 * 分类查询参数
 */
export interface CategoryQuery {
  pageSize: number;
  pageNum: number;
}

/**
 * 用户查询参数
 */
export interface AuthorQuery {
  Name?: string;
  PageNum?: number;
  PageSize?: number;
}

/**
 * 文章新增参数
 */
export interface ArticleInsert {
  categoryId: Id;
  title: string;
  content: string;
  status?: number;
}

/**
 * 文章更新参数
 */
export interface ArticleUpdate {
  id: Id;
  authorId: Id;
  categoryId?: Id;
  title?: string;
  content?: string;
}

/**
 * 文章删除参数
 */
export interface ArticleDelete {
  id: Id;
  authorId: Id;
}

/**
 * 文章状态更新参数
 */
export interface ArticleStatusUpdate {
  id: Id;
  authorId: Id;
}

/**
 * 评论新增参数
 */
export interface CommentInsert {
  articleId: Id;
  content: string;
  parentId?: Id;
}

/**
 * 关注操作参数
 */
export interface FollowParams {
  authorId: Id;
  targetId: Id;
}

/**
 * ID 参数
 */
export interface IdParam {
  id: Id;
}

/**
 * 用户更新参数
 */
export interface AuthorUpdate {
  id: Id;
  username?: string;
  password?: string;
}

/**
 * 分类新增参数
 */
export interface CategoryInsert {
  name: string;
}

/**
 * 分类更新参数
 */
export interface CategoryUpdate {
  id: Id;
  name: string;
}

/**
 * AI 操作类型
 */
export type AIActionType = 'polish' | 'continue' | 'summarize';

/**
 * AI 目标字段
 */
export type AIFieldTarget = 'title' | 'content';

/**
 * AI 内容作用范围
 */
export type AIContentScope = 'full' | 'selection';

/**
 * AI 结果应用模式
 */
export type AIApplyMode = 'replace' | 'append' | 'replace-selection' | 'insert-after-selection';

/**
 * AI 请求状态
 */
export type AIRequestStatus = 'idle' | 'streaming' | 'success' | 'error';
