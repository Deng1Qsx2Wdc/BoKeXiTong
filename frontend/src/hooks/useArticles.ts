import { useState, useEffect, useCallback } from 'react';
import { articleQuery } from '../api/article';
import type { Article, ArticleQuery } from '../types';

interface UseArticlesResult {
  articles: Article[];
  total: number;
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

export const useArticles = (params: ArticleQuery): UseArticlesResult => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tick, setTick] = useState(0);

  const refetch = useCallback(() => setTick((t) => t + 1), []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    articleQuery(params)
      .then((res) => {
        if (cancelled) return;
        const page = res.data.data;
        setArticles(page?.records ?? []);
        setTotal(page?.total ?? 0);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err?.message ?? '加载失败');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.pageNum, params.pageSize, params.categoryId, params.authorId, params.status, params.keyword, tick]);

  return { articles, total, loading, error, refetch };
};
