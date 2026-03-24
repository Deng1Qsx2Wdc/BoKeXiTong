import { useState, useEffect } from 'react';
import { categoryQuery } from '../api/category';
import type { Category } from '../types';
import { PAGINATION } from '../utils/constants';

interface UseCategoriesResult {
  categories: Category[];
  categoryMap: Record<string, Category>;
  loading: boolean;
}

let cache: { categories: Category[]; map: Record<string, Category> } | null = null;

export const useCategories = (): UseCategoriesResult => {
  const [categories, setCategories] = useState<Category[]>(cache?.categories ?? []);
  const [categoryMap, setCategoryMap] = useState<Record<string, Category>>(cache?.map ?? {});
  const [loading, setLoading] = useState(!cache);

  useEffect(() => {
    if (cache) return;
    categoryQuery({ pageNum: PAGINATION.DEFAULT_PAGE, pageSize: PAGINATION.CATEGORY_OPTIONS_PAGE_SIZE })
      .then((res) => {
        const cats = res.data.data?.records ?? [];
        const map: Record<string, Category> = {};
        cats.forEach((c) => {
          map[c.id] = c;
        });
        cache = { categories: cats, map };
        setCategories(cats);
        setCategoryMap(map);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return { categories, categoryMap, loading };
};
