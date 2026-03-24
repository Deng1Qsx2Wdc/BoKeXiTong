import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { articleQuery } from '../../api/article';
import { categoryQueryOne } from '../../api/category';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { ArticleCard } from '../../components/article/ArticleCard';
import { Pagination } from '../../components/common/Pagination';
import { Loading } from '../../components/common/Loading';
import type { Article, Category } from '../../types';
import { ARTICLE_STATUS, PAGINATION } from '../../utils/constants';
import styles from './CategoryBrowse.module.css';

const CategoryBrowse: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [articles, setArticles] = useState<Article[]>([]);
  const [category, setCategory] = useState<Category | null>(null);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const pageSize = PAGINATION.DEFAULT_PAGE_SIZE;

  useEffect(() => {
    if (!id) return;
    categoryQueryOne(id)
      .then((res) => {
        setCategory(res.data.data ?? null);
      })
      .catch(() => {});
  }, [id]);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    articleQuery({
      categoryId: id,
      status: ARTICLE_STATUS.PUBLISHED,
      pageNum: page,
      pageSize,
    })
      .then((res) => {
        const p = res.data.data;
        setArticles(p?.records ?? []);
        setTotal(p?.total ?? 0);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [id, page]);

  return (
    <PageLayout>
      <Container>
        <div className={styles.hero}>
          <h1 className={styles['hero-title']}>{category?.name ?? '分类'}</h1>
          <p className={styles['hero-count']}>{total} 篇文章</p>
        </div>

        {loading ? (
          <Loading />
        ) : articles.length === 0 ? (
          <p className={styles.empty}>该分类下暂无文章</p>
        ) : (
          <>
            <div className={styles.list}>
              {articles.map((article) => (
                <ArticleCard key={article.id} article={article} />
              ))}
            </div>
            <Pagination
              current={page}
              total={total}
              pageSize={pageSize}
              onChange={setPage}
            />
          </>
        )}
      </Container>
    </PageLayout>
  );
};

export default CategoryBrowse;
