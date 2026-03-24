import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { authorQueryOneById } from '../../api/author';
import { articleQuery } from '../../api/article';
import { followsFollow, followsUnfollow, followsCheckOne } from '../../api/follows';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Button } from '../../components/common/Button';
import { Loading } from '../../components/common/Loading';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { ARTICLE_STATUS, PAGINATION, ROUTES } from '../../utils/constants';
import { formatDate } from '../../utils/format';
import type { Author, Article } from '../../types';
import styles from './UserAuthorPage.module.css';

const UserAuthorPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user, isAuthenticated } = useAuth();
  const { success, error } = useToast();
  const [author, setAuthor] = useState<Author | null>(null);
  const [articles, setArticles] = useState<Article[]>([]);
  const [isFollowing, setIsFollowing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    authorQueryOneById(id)
      .then(async (res) => {
        const authorData = res.data.data;
        if (!authorData) return;

        setAuthor(authorData);

        const articleRes = await articleQuery({
          authorId: authorData.id,
          status: ARTICLE_STATUS.PUBLISHED,
          pageNum: PAGINATION.DEFAULT_PAGE,
          pageSize: PAGINATION.AUTHOR_PAGE_SIZE,
        });
        setArticles(articleRes.data.data?.records ?? []);

        if (isAuthenticated && user?.id && user.id !== authorData.id) {
          const followRes = await followsCheckOne({ authorId: user.id, targetId: authorData.id });
          setIsFollowing(!!followRes.data.data);
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [id, isAuthenticated, user?.id]);

  const handleFollow = async () => {
    if (!user?.id || !author?.id) return;
    try {
      if (isFollowing) {
        await followsUnfollow({ authorId: user.id, targetId: author.id });
        setIsFollowing(false);
        success('已取消关注');
      } else {
        await followsFollow({ authorId: user.id, targetId: author.id });
        setIsFollowing(true);
        success('关注成功');
      }
    } catch {
      error('操作失败');
    }
  };

  if (loading) return <PageLayout><Loading /></PageLayout>;
  if (!author) return <PageLayout><Container><p>用户不存在</p></Container></PageLayout>;

  return (
    <PageLayout>
      <Container>
        <div className={styles.hero}>
          <div className={styles.avatar}>{author.username?.[0]?.toUpperCase()}</div>
          <div className={styles.info}>
            <h1 className={styles.username}>{author.username}</h1>
            <p className={styles.count}>{articles.length} 篇文章</p>
          </div>
          {isAuthenticated && user?.id !== author.id && (
            <Button variant={isFollowing ? 'outline' : 'primary'} onClick={handleFollow}>
              {isFollowing ? '取消关注' : '关注'}
            </Button>
          )}
        </div>
        <div className={styles.articles}>
          {articles.map((article) => (
            <Link key={article.id} to={ROUTES.ARTICLE_DETAIL.replace(':id', String(article.id))} className={styles.item}>
              <span className={styles['item-title']}>{article.title}</span>
              <span className={styles['item-date']}>{formatDate(article.createTime)}</span>
            </Link>
          ))}
          {articles.length === 0 && <p className={styles.empty}>该用户还没有发布文章</p>}
        </div>
      </Container>
    </PageLayout>
  );
};

export default UserAuthorPage;
