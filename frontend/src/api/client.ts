import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import type { Result } from '../types';
import { clearAuth, getToken } from '../utils/token';
import { API_BASE_URL, API_TIMEOUT } from '../utils/constants';

/**
 * API 基础配置
 */
export const getApiBaseUrl = () => API_BASE_URL;

export const getAuthToken = () => getToken();

export const getErrorMessage = (error: unknown, fallback = '请求失败') => {
  if (axios.isAxiosError<Result>(error)) {
    return error.response?.data?.msg || error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message || fallback;
  }

  if (
    typeof error === 'object'
    && error !== null
    && 'message' in error
    && typeof error.message === 'string'
  ) {
    return error.message;
  }

  return fallback;
};

/**
 * 创建 Axios 实例
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
  // Treat large integers as strings to avoid JS precision loss on 64-bit IDs
  transformResponse: [(data) => {
    if (typeof data === 'string') {
      try {
        return JSON.parse(data.replace(/(:\s*)(\d{16,})/g, '$1"$2"'));
      } catch {
        return data;
      }
    }
    return data;
  }],
});

/**
 * 请求拦截器
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 获取 token
    const token = getAuthToken();

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error: AxiosError) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 响应拦截器
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const { data } = response;

    // 检查业务状态码
    if (data.code === 200) {
      return response;
    }

    // 处理业务错误
    console.error('业务错误:', data.msg || '未知错误');
    return Promise.reject(new Error(data.msg || '请求失败'));
  },
  (error: AxiosError<Result>) => {
    // 处理 HTTP 错误
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // 未授权，清除 token 并跳转到登录页
          clearAuth();
          console.error('未授权，请重新登录');
          // 可以在这里添加路由跳转逻辑
          // window.location.href = '/login';
          break;
        case 403:
          console.error('没有权限访问该资源');
          break;
        case 404:
          console.error('请求的资源不存在');
          break;
        case 500:
          console.error('服务器内部错误');
          break;
        default:
          console.error(`请求失败: ${data?.msg || error.message}`);
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接');
    } else {
      console.error('请求配置错误:', error.message);
    }

    return Promise.reject(error);
  }
);

export default apiClient;
