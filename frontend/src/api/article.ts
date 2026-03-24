import apiClient from './client';
import type {
  Result,
  Article,
  Page,
  ArticleQuery,
  ArticleInsert,
  ArticleUpdate,
  ArticleDelete,
  ArticleStatusUpdate,
} from '../types';

/**
 * 文章模块 API
 */

/**
 * 添加文章
 * @param data 文章数据
 */
export const articleInsert = (data: ArticleInsert) => {
  return apiClient.post<Result<void>>('/article/insert', data);
};

/**
 * 删除文章
 * @param data 文章ID和作者ID
 */
export const articleDelete = (data: ArticleDelete) => {
  return apiClient.post<Result<void>>('/article/delete', data);
};

/**
 * 查询文章列表（多条件分页）
 * @param data 查询条件
 */
export const articleQuery = (data: ArticleQuery) => {
  return apiClient.post<Result<Page<Article>>>('/article/query', data);
};

/**
 * 查询单篇文章详情
 * @param id 文章ID
 */
export const articleQueryOne = (id: string | number) => {
  return apiClient.post<Result<Article>>('/article/queryone', { id: String(id) });
};

/**
 * 更新文章
 * @param data 文章更新数据
 */
export const articleUpdate = (data: ArticleUpdate) => {
  return apiClient.post<Result<void>>('/article/update', data);
};

/**
 * 改为草稿
 * @param data 文章ID和作者ID
 */
export const articleDraft = (data: ArticleStatusUpdate) => {
  return apiClient.post<Result<void>>('/article/draft', data);
};

/**
 * 发布文章
 * @param data 文章ID和作者ID
 */
export const articlePublic = (data: ArticleStatusUpdate) => {
  return apiClient.post<Result<void>>('/article/public', data);
};

/**
 * 下架文章
 * @param data 文章ID和作者ID
 */
export const articleOffline = (data: ArticleStatusUpdate) => {
  return apiClient.post<Result<void>>('/article/offline', data);
};
