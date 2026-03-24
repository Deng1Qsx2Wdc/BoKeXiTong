import apiClient from './client';
import type { Result, Dashboard, Cate_Ari_Total } from '../types';

/**
 * 仪表盘模块 API
 */

/**
 * 获取系统统计数据
 */
export const dashboardGetAllTotal = () => {
  return apiClient.get<Result<Dashboard>>('/dashboard/alltotal');
};

/**
 * 获取各分类的文章统计
 * @param categoryId 分类ID
 */
export const dashboardGetCategoryArticleTotal = (categoryId: number) => {
  return apiClient.get<Result<Cate_Ari_Total[]>>('/dashboard/category_article_total', {
    params: { categoryId },
  });
};
