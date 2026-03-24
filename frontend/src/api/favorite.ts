import apiClient from './client';
import type { Result, Id } from '../types';

/**
 * 收藏模块 API
 */

/**
 * 收藏/取消收藏文章
 * @param id 文章 ID
 */
export const favoriteToggle = (id: Id) => {
  return apiClient.post<Result<void>>('/favorite/favorites', { id });
};

/**
 * 获取当前用户的收藏列表
 */
export const favoriteMyList = () => {
  return apiClient.post<Result<Id[]>>('/favorite/author_favorites_list');
};
