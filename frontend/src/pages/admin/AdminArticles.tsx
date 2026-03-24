import React, { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  adminArticleDelete,
  adminArticleDraft,
  adminArticleOffline,
  adminArticlePublic,
  adminArticleQuery,
} from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { Loading } from '../../components/common/Loading';
import { Pagination } from '../../components/common/Pagination';
import { Container } from '../../components/layout/Container';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { useToast } from '../../context/ToastContext';
import { ARTICLE_STATUS, ARTICLE_STATUS_TEXT, PAGINATION, ROUTES } from '../../utils/constants';
import { formatDate } from '../../utils/format';
import type { Article } from '../../types';
import styles from './AdminArticles.module.css';

const STATUS_LABELS: Record<number, string> = {
  [ARTICLE_STATUS.DRAFT]: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.DRAFT],
  [ARTICLE_STATUS.PUBLISHED]: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.PUBLISHED],
  [ARTICLE_STATUS.OFFLINE]: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.OFFLINE],
};

const FILTERS = [
  { label: '全部', value: undefined },
  { label: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.DRAFT], value: ARTICLE_STATUS.DRAFT },
  { label: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.PUBLISHED], value: ARTICLE_STATUS.PUBLISHED },
  { label: ARTICLE_STATUS_TEXT[ARTICLE_STATUS.OFFLINE], value: ARTICLE_STATUS.OFFLINE },
];

const AdminArticles: React.FC = () => {
  const { success, error } = useToast();
  const [articles, setArticles] = useState<Article[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState<number>(PAGINATION.DEFAULT_PAGE);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<number | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const pageSize = PAGINATION.ADMIN_PAGE_SIZE;

  const fetchArticles = useCallback(() => {
    setLoading(true);
    adminArticleQuery({
      pageNum: page,
      pageSize,
      ...(keyword.trim() ? { keyword: keyword.trim() } : {}),
      ...(status !== undefined ? { status } : {}),
    })
      .then((response) => {
        setArticles(response.data.data?.records ?? []);
        setTotal(response.data.data?.total ?? 0);
      })
      .catch((err: unknown) => error(getErrorMessage(err, '加载文章失败')))
      .finally(() => setLoading(false));
  }, [error, keyword, page, status]);

  useEffect(() => {
    fetchArticles();
  }, [fetchArticles]);

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定删除这篇文章吗？此操作不可恢复。')) return;

    try {
      await adminArticleDelete(id);
      success('文章已删除');
      fetchArticles();
    } catch (err: unknown) {
      error(getErrorMessage(err, '删除失败'));
    }
  };

  const handleStatus = async (article: Article, action: 'publish' | 'draft' | 'offline') => {
    try {
      if (action === 'publish') {
        await adminArticlePublic(article.id);
      } else if (action === 'draft') {
        await adminArticleDraft(article.id);
      } else {
        await adminArticleOffline(article.id);
      }

      success('文章状态已更新');
      fetchArticles();
    } catch (err: unknown) {
      error(getErrorMessage(err, '状态更新失败'));
    }
  };

  return (
    <PageLayout>
      <Container>
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>文章管理</h1>
            <p className={styles.subtitle}>
              管理员可统一查看、编辑、发布、下架和删除站内文章，并可按标题、正文、文章 ID、作者 ID 检索。
            </p>
          </div>
        </div>

        <div className={styles.toolbar}>
          <Input
            placeholder="搜索标题 / 正文 / 文章ID / 作者ID"
            value={keyword}
            onChange={(event) => {
              setKeyword((event.target as HTMLInputElement).value);
              setPage(PAGINATION.DEFAULT_PAGE);
            }}
          />
          <p className={styles['search-hint']}>支持标题、正文模糊检索，数字可匹配文章 ID 或作者 ID。</p>
        </div>

        <div className={styles.filters}>
          {FILTERS.map((filter) => (
            <button
              key={filter.label}
              type="button"
              className={`${styles.filter} ${status === filter.value ? styles['filter-active'] : ''}`}
              onClick={() => {
                setStatus(filter.value);
                setPage(PAGINATION.DEFAULT_PAGE);
              }}
            >
              {filter.label}
            </button>
          ))}
        </div>

        {loading ? <Loading /> : (
          <>
            <div className={styles.list}>
              {articles.map((article) => (
                <article key={article.id} className={styles.card}>
                  <div className={styles.main}>
                    <Link to={ROUTES.ARTICLE_DETAIL.replace(':id', String(article.id))} className={styles.titleLink}>
                      {article.title}
                    </Link>
                    <div className={styles.meta}>
                      <span>作者 ID: {article.authorId}</span>
                      <span>{STATUS_LABELS[article.status] ?? '未知状态'}</span>
                      <span>{formatDate(article.createTime)}</span>
                    </div>
                  </div>
                  <div className={styles.actions}>
                    <Link to={ROUTES.ADMIN_ARTICLE_EDIT.replace(':id', String(article.id))}>
                      <Button size="small" variant="outline">编辑</Button>
                    </Link>
                    {article.status !== ARTICLE_STATUS.PUBLISHED && (
                      <Button size="small" variant="ghost" onClick={() => handleStatus(article, 'publish')}>发布</Button>
                    )}
                    {article.status !== ARTICLE_STATUS.DRAFT && (
                      <Button size="small" variant="ghost" onClick={() => handleStatus(article, 'draft')}>转草稿</Button>
                    )}
                    {article.status === ARTICLE_STATUS.PUBLISHED && (
                      <Button size="small" variant="ghost" onClick={() => handleStatus(article, 'offline')}>下架</Button>
                    )}
                    <Button size="small" variant="ghost" onClick={() => handleDelete(article.id)}>删除</Button>
                  </div>
                </article>
              ))}
            </div>
            <Pagination current={page} total={total} pageSize={pageSize} onChange={setPage} />
          </>
        )}
      </Container>
    </PageLayout>
  );
};

export default AdminArticles;
