import React, { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { articleDelete, articleDraft, articleOffline, articlePublic, articleQuery } from '../../api/article';
import { Loading } from '../../components/common/Loading';
import { Button } from '../../components/common/Button';
import { Container } from '../../components/layout/Container';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { ARTICLE_STATUS, ARTICLE_STATUS_TEXT, PAGINATION, ROUTES } from '../../utils/constants';
import { formatDate } from '../../utils/format';
import type { Article, Id } from '../../types';
import styles from './UserArticles.module.css';

const STATUS_COLORS: Record<number, string> = {
  [ARTICLE_STATUS.DRAFT]: '#888',
  [ARTICLE_STATUS.PUBLISHED]: '#16a34a',
  [ARTICLE_STATUS.OFFLINE]: '#c73e1d',
};

const parseStatus = (value: string | null) => {
  if (value === null || value === '') return undefined;
  const parsed = Number(value);

  if (
    parsed === ARTICLE_STATUS.DRAFT
    || parsed === ARTICLE_STATUS.PUBLISHED
    || parsed === ARTICLE_STATUS.OFFLINE
  ) {
    return parsed;
  }

  return undefined;
};

const UserArticles: React.FC = () => {
  const { user } = useAuth();
  const { success, error } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);

  const selectedStatus = parseStatus(searchParams.get('status'));

  const filters = useMemo(() => ([
    { label: '全部', value: undefined },
    { label: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.DRAFT], value: ARTICLE_STATUS.DRAFT },
    { label: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.PUBLISHED], value: ARTICLE_STATUS.PUBLISHED },
    { label: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.OFFLINE], value: ARTICLE_STATUS.OFFLINE },
  ]), []);

  const fetchArticles = () => {
    if (!user?.id) return;
    setLoading(true);

    articleQuery({
      authorId: user.id,
      pageNum: PAGINATION.DEFAULT_PAGE,
      pageSize: PAGINATION.LARGE_PAGE_SIZE,
      ...(selectedStatus !== undefined ? { status: selectedStatus } : {}),
    })
      .then((res) => setArticles(res.data.data?.records ?? []))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchArticles();
  }, [selectedStatus, user?.id]);

  const setStatusFilter = (status?: number) => {
    const nextParams = new URLSearchParams(searchParams);

    if (status === undefined) {
      nextParams.delete('status');
    } else {
      nextParams.set('status', String(status));
    }

    setSearchParams(nextParams);
  };

  const handleDelete = async (id: Id) => {
    if (!user?.id || !confirm('确定删除这篇文章吗？')) return;
    try {
      await articleDelete({ id, authorId: user.id });
      success('删除成功');
      fetchArticles();
    } catch {
      error('删除失败');
    }
  };

  const handleStatus = async (article: Article, action: 'publish' | 'draft' | 'offline') => {
    if (!user?.id) return;
    const params = { id: article.id, authorId: user.id };
    try {
      if (action === 'publish') await articlePublic(params);
      else if (action === 'draft') await articleDraft(params);
      else await articleOffline(params);
      success('操作成功');
      fetchArticles();
    } catch {
      error('操作失败');
    }
  };

  if (loading) return <PageLayout><Loading /></PageLayout>;

  return (
    <PageLayout>
      <Container>
        <div className={styles.header}>
          <h1 className={styles.title}>我的文章</h1>
          <Link to={ROUTES.ARTICLE_NEW}><Button variant="primary">写新文章</Button></Link>
        </div>

        <div className={styles.filters}>
          {filters.map((filter) => (
            <button
              key={filter.label}
              type="button"
              className={`${styles.filter} ${selectedStatus === filter.value ? styles['filter-active'] : ''}`}
              onClick={() => setStatusFilter(filter.value)}
            >
              {filter.label}
            </button>
          ))}
        </div>

        {articles.length === 0 ? (
          <p className={styles.empty}>还没有文章，<Link to={ROUTES.ARTICLE_NEW}>写第一篇</Link></p>
        ) : (
          <div className={styles.list}>
            {articles.map((article) => (
              <div key={article.id} className={styles.item}>
                <div className={styles['item-main']}>
                  <Link to={ROUTES.ARTICLE_DETAIL.replace(':id', String(article.id))} className={styles['item-title']}>
                    {article.title}
                  </Link>
                  <div className={styles.meta}>
                    <span style={{ color: STATUS_COLORS[article.status] }}>
                      {ARTICLE_STATUS_TEXT[article.status as keyof typeof ARTICLE_STATUS_TEXT]}
                    </span>
                    <span>{formatDate(article.createTime)}</span>
                    <span>赞 {article.thumbsUp}</span>
                    <span>藏 {article.favorites}</span>
                  </div>
                </div>
                <div className={styles.actions}>
                  <Link to={ROUTES.ARTICLE_EDIT.replace(':id', String(article.id))}>
                    <Button size="small" variant="outline">编辑</Button>
                  </Link>
                  {article.status !== ARTICLE_STATUS.PUBLISHED && (
                    <Button size="small" variant="ghost" onClick={() => handleStatus(article, 'publish')}>发布</Button>
                  )}
                  {article.status === ARTICLE_STATUS.PUBLISHED && (
                    <Button size="small" variant="ghost" onClick={() => handleStatus(article, 'offline')}>下架</Button>
                  )}
                  {article.status !== ARTICLE_STATUS.DRAFT && (
                    <Button size="small" variant="ghost" onClick={() => handleStatus(article, 'draft')}>存草稿</Button>
                  )}
                  <Button size="small" variant="ghost" onClick={() => handleDelete(article.id)}>删除</Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Container>
    </PageLayout>
  );
};

export default UserArticles;
