import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { authorAllMessage } from '../../api/author';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Loading } from '../../components/common/Loading';
import { ROUTES, USER_ROLE, USER_ROLE_TEXT } from '../../utils/constants';
import { formatDate } from '../../utils/format';
import { useAuth } from '../../context/AuthContext';
import type { Article, Comment, Favorites, Thumbs_up } from '../../types';
import styles from './UserProfile.module.css';

const UserProfile: React.FC = () => {
  const { user } = useAuth();
  const [articles, setArticles] = useState<Article[]>([]);
  const [thumbsUp, setThumbsUp] = useState<Thumbs_up[]>([]);
  const [favorites, setFavorites] = useState<Favorites[]>([]);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    authorAllMessage()
      .then((res) => {
        const data = res.data.data;
        if (!data) return;
        setArticles(data[0] ?? []);
        setThumbsUp(data[1] ?? []);
        setFavorites(data[2] ?? []);
        setComments(data[3] ?? []);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageLayout><Loading /></PageLayout>;

  const roleText = user?.role === USER_ROLE.ADMIN ? USER_ROLE_TEXT[USER_ROLE.ADMIN] : '作者';

  return (
    <PageLayout>
      <Container>
        <div className={styles.profile}>
          <div className={styles.hero}>
            <div className={styles.avatar}>{user?.username?.[0]?.toUpperCase() ?? 'U'}</div>
            <div>
              <h1 className={styles.username}>{user?.username}</h1>
              <p className={styles.role}>{roleText}</p>
            </div>
          </div>

          <div className={styles.stats}>
            <div className={styles.stat}>
              <span className={styles['stat-num']}>{articles.length}</span>
              <span className={styles['stat-label']}>文章</span>
            </div>
            <div className={styles.stat}>
              <span className={styles['stat-num']}>{thumbsUp.filter((item) => item.status === 1).length}</span>
              <span className={styles['stat-label']}>获赞</span>
            </div>
            <div className={styles.stat}>
              <span className={styles['stat-num']}>{favorites.filter((item) => item.status === 1).length}</span>
              <span className={styles['stat-label']}>收藏</span>
            </div>
            <div className={styles.stat}>
              <span className={styles['stat-num']}>{comments.length}</span>
              <span className={styles['stat-label']}>评论</span>
            </div>
          </div>

          <nav className={styles.nav}>
            <Link to={ROUTES.PROFILE_ARTICLES} className={styles['nav-link']}>我的文章</Link>
            <Link to={ROUTES.PROFILE_FAVORITES} className={styles['nav-link']}>我的收藏</Link>
            <Link to={ROUTES.PROFILE_FOLLOWS} className={styles['nav-link']}>关注</Link>
            <Link to={ROUTES.PROFILE_SETTINGS} className={styles['nav-link']}>设置</Link>
          </nav>

          <div className={styles.recent}>
            <h2 className={styles['section-title']}>最近文章</h2>
            {articles.length === 0 ? (
              <p className={styles.empty}>还没有发布文章</p>
            ) : (
              articles.slice(0, 5).map((article) => (
                <Link
                  key={article.id}
                  to={ROUTES.ARTICLE_DETAIL.replace(':id', String(article.id))}
                  className={styles['article-item']}
                >
                  <span className={styles['article-title']}>{article.title}</span>
                  <span className={styles['article-date']}>{formatDate(article.createTime)}</span>
                </Link>
              ))
            )}
          </div>
        </div>
      </Container>
    </PageLayout>
  );
};

export default UserProfile;
