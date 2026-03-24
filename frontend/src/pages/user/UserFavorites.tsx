import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { favoriteMyList } from '../../api/favorite';
import { articleQueryOne } from '../../api/article';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Loading } from '../../components/common/Loading';
import type { Article, Id } from '../../types';
import { ROUTES } from '../../utils/constants';
import styles from './UserFavorites.module.css';

const UserFavorites: React.FC = () => {
  const [favoriteIds, setFavoriteIds] = useState<Id[]>([]);
  const [articles, setArticles] = useState<Record<Id, Article>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    favoriteMyList()
      .then(async (res) => {
        const ids = res.data.data ?? [];
        setFavoriteIds(ids);
        const map: Record<Id, Article> = {};
        await Promise.allSettled(
          ids.map(async (articleId) => {
            const r = await articleQueryOne(articleId);
            if (r.data.data) {
              map[articleId] = r.data.data;
            }
          }),
        );
        setArticles(map);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageLayout><Loading /></PageLayout>;

  return (
    <PageLayout>
      <Container>
        <h1 className={styles.title}>我的收藏</h1>
        {favoriteIds.length === 0 ? (
          <p className={styles.empty}>还没有收藏任何文章</p>
        ) : (
          <div className={styles.list}>
            {favoriteIds.map((articleId) => {
              const article = articles[articleId];
              return (
                <Link key={articleId} to={ROUTES.ARTICLE_DETAIL.replace(':id', String(articleId))} className={styles.item}>
                  <span className={styles['item-title']}>{article?.title ?? `文章 #${articleId}`}</span>
                </Link>
              );
            })}
          </div>
        )}
      </Container>
    </PageLayout>
  );
};

export default UserFavorites;
