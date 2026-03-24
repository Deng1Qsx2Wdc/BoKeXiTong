import apiClient from './client';
import type {
  Result,
  Login,
  AdminLoginResponse,
  Author,
  Page,
  Id,
  Category,
  CategoryInsert,
  CategoryUpdate,
  CategoryQuery,
  Article,
  ArticleQuery,
  ArticleUpdate,
} from '../types';
import { PAGINATION } from '../utils/constants';

export const adminLogin = (data: Login) => {
  return apiClient.post<Result<AdminLoginResponse>>('/admin/login', data);
};

export const adminListUsers = (
  pageNum: number = PAGINATION.DEFAULT_PAGE,
  pageSize: number = PAGINATION.DEFAULT_PAGE_SIZE,
  Name = '',
) => {
  return apiClient.post<Result<Page<Author>>>('/admin/list', null, {
    params: { pageNum, pageSize, Name },
  });
};

export const adminDeleteUser = (id: Id) => {
  return apiClient.post<Result<void>>('/admin/users/delete', { id });
};

export const adminCategoryQuery = (data: CategoryQuery) => {
  return apiClient.post<Result<Page<Category>>>('/admin/categories/query', data);
};

export const adminCategoryInsert = (data: CategoryInsert) => {
  return apiClient.post<Result<void>>('/admin/categories/insert', data);
};

export const adminCategoryUpdate = (data: CategoryUpdate) => {
  return apiClient.post<Result<void>>('/admin/categories/update', data);
};

export const adminCategoryDelete = (id: Id) => {
  return apiClient.post<Result<void>>('/admin/categories/delete', { id });
};

export const adminArticleQuery = (data: ArticleQuery) => {
  return apiClient.post<Result<Page<Article>>>('/admin/articles/query', data);
};

export const adminArticleQueryOne = (id: Id) => {
  return apiClient.post<Result<Article>>('/admin/articles/queryone', { id });
};

export const adminArticleUpdate = (data: ArticleUpdate) => {
  return apiClient.post<Result<void>>('/admin/articles/update', data);
};

export const adminArticleDelete = (id: Id) => {
  return apiClient.post<Result<void>>('/admin/articles/delete', { id });
};

export const adminArticleDraft = (id: Id) => {
  return apiClient.post<Result<void>>('/admin/articles/draft', { id });
};

export const adminArticlePublic = (id: Id) => {
  return apiClient.post<Result<void>>('/admin/articles/public', { id });
};

export const adminArticleOffline = (id: Id) => {
  return apiClient.post<Result<void>>('/admin/articles/offline', { id });
};
