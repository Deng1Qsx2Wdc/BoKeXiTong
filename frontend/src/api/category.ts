import apiClient from './client';
import type {
  Result,
  Category,
  Page,
  CategoryQuery,
  CategoryInsert,
  CategoryUpdate,
  Id,
} from '../types';

/**
 * 分类模块 API
 */

/**
 * 添加分类
 * @param data 分类名称
 */
export const categoryInsert = (data: CategoryInsert) => {
  return apiClient.post<Result<void>>('/category/insert', data);
};

/**
 * 删除分类
 * @param id 分类 ID
 */
export const categoryDelete = (id: Id) => {
  return apiClient.post<Result<void>>('/category/delete', { id });
};

/**
 * 查询分类列表
 * @param data 分页参数
 */
export const categoryQuery = (data: CategoryQuery) => {
  return apiClient.post<Result<Page<Category>>>('/category/query', data);
};

/**
 * 查询单个分类
 * @param id 分类 ID
 */
export const categoryQueryOne = (id: Id) => {
  return apiClient.get<Result<Category>>('/category/queryone', {
    params: { id },
  });
};

/**
 * 更新分类
 * @param data 分类更新数据
 */
export const categoryUpdate = (data: CategoryUpdate) => {
  return apiClient.post<Result<void>>('/category/update', data);
};
