import apiClient from './client';
import type { Result, Comment, CommentInsert } from '../types';

/**
 * 评论模块 API
 */

/**
 * 添加评论
 * @param data 评论数据
 */
export const commentSet = (data: CommentInsert) => {
  return apiClient.post<Result<void>>('/comment/setComment', data);
};

/**
 * 获取文章评论列表（树形结构）
 * @param articleId 文章ID
 */
export const commentGetList = (articleId: string | number) => {
  return apiClient.post<Result<Comment[]>>('/comment/getCommentList', { articleId: String(articleId) });
};
