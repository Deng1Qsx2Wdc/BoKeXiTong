import apiClient from './client';
import type {
  Result,
  Login,
  AuthorLoginResponse,
  Author,
  Page,
  AuthorUpdate,
  Article,
  Thumbs_up,
  Favorites,
  Comment,
  Id,
} from '../types';

/**
 * 用户模块 API
 */

/**
 * 用户登录
 * @param data 登录信息
 */
export const authorLogin = (data: Login) => {
  return apiClient.post<Result<AuthorLoginResponse>>('/author/login', data);
};

/**
 * 用户注册
 * @param data 注册信息
 */
export const authorRegister = (data: Login) => {
  return apiClient.post<Result<void>>('/author/register', data);
};

/**
 * 删除用户
 * @param id 用户 ID
 */
export const authorDelete = (id: Id) => {
  return apiClient.post<Result<void>>('/author/delete', { id });
};

/**
 * 查询用户列表
 * @param Name 用户名（模糊搜索）
 * @param PageNum 页码
 * @param PageSize 每页大小
 */
export const authorQuery = (Name = '', PageNum = 1, PageSize = 10) => {
  return apiClient.get<Result<Page<Author>>>('/author/query', {
    params: { Name, PageNum, PageSize },
  });
};

/**
 * 查询单个用户
 * @param username 用户名
 */
export const authorQueryOne = (username: string) => {
  return apiClient.get<Result<Author>>('/author/queryone', {
    params: { username },
  });
};

/**
 * 按用户 ID 查询单个用户
 * @param id 用户 ID
 */
export const authorQueryOneById = (id: Id) => {
  return apiClient.get<Result<Author>>('/author/querybyid', {
    params: { id },
  });
};

/**
 * 更新用户信息
 * @param data 用户更新数据
 */
export const authorUpdate = (data: AuthorUpdate) => {
  return apiClient.post<Result<void>>('/author/update', data);
};

/**
 * 获取用户所有信息
 * @returns [文章列表, 点赞列表, 收藏列表, 评论列表]
 */
export const authorAllMessage = () => {
  return apiClient.post<Result<[Article[], Thumbs_up[], Favorites[], Comment[]]>>('/author/allmessage');
};
