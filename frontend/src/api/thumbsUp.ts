import apiClient from './client';
import type { Result, Author, Id } from '../types';

/**
 * 点赞模块 API
 */

/**
 * 点赞/取消点赞文章
 * @param id 文章 ID
 */
export const thumbsUpToggle = (id: Id) => {
  return apiClient.post<Result<void>>('/thumbs_up/thumbs', { id });
};

/**
 * 获取点赞某篇文章的用户列表
 * @param id 文章 ID
 */
export const thumbsUpAuthorList = (id: Id) => {
  return apiClient.post<Result<Author[]>>('/thumbs_up/thumbs_author_list', { id });
};

/**
 * 获取当前用户的点赞列表
 */
export const thumbsUpMyList = () => {
  return apiClient.post<Result<Id[]>>('/thumbs_up/author_thumbs_list');
};
