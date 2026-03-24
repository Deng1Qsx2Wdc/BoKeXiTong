import apiClient from './client';
import type { Result, Follows, Page, Article, FollowParams, Id } from '../types';

/**
 * 关注模块 API
 */

/**
 * 获取用户的关注列表
 * @param id 用户 ID
 */
export const followsGetFollowList = (id: Id) => {
  return apiClient.post<Result<Page<Follows>>>('/follows/followList', { id });
};

/**
 * 查询是否关注某个用户
 * @param params 关注参数
 */
export const followsCheckOne = (params: FollowParams) => {
  return apiClient.post<Result<Follows | null>>('/follows/followOne', params);
};

/**
 * 获取用户的粉丝列表
 * @param id 用户 ID
 */
export const followsGetFollowerList = (id: Id) => {
  return apiClient.post<Result<Follows[]>>('/follows/followerList', { id });
};

/**
 * 关注用户
 * @param params 关注参数
 */
export const followsFollow = (params: FollowParams) => {
  return apiClient.post<Result<void>>('/follows/follow', params);
};

/**
 * 取消关注
 * @param params 关注参数
 */
export const followsUnfollow = (params: FollowParams) => {
  return apiClient.post<Result<void>>('/follows/unfollow', params);
};

/**
 * 获取关注用户的文章列表
 * @param Page 页码
 * @param PageSize 每页大小
 */
export const followsGetArticles = (Page = 1, PageSize = 10) => {
  return apiClient.get<Result<Page<Article>>>('/follows/foll', {
    params: { Page, PageSize },
  });
};
