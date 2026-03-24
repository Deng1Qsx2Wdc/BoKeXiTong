import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { articleQueryOne, articleDelete } from '../../api/article';
import { getErrorMessage } from '../../api/client';
import { thumbsUpMyList, thumbsUpToggle } from '../../api/thumbsUp';
import { favoriteMyList, favoriteToggle } from '../../api/favorite';
import { commentGetList } from '../../api/comment';
import { categoryQueryOne } from '../../api/category';
import { followsCheckOne, followsFollow, followsUnfollow } from '../../api/follows';
import { authorQueryOneById } from '../../api/author';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Loading } from '../../components/common/Loading';
import { Modal } from '../../components/common/Modal';
import { Button } from '../../components/common/Button';
import { CommentSection } from '../../components/comment/CommentSection/CommentSection';
import type { Article, Category, Author, Comment } from '../../types';
import { ROUTES } from '../../utils/constants';
import { formatDate } from '../../utils/format';
import { getSafeRichTextHtml } from '../../utils/richText';
import styles from './ArticleDetail.module.css';

const ArticleDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const { success, error } = useToast();

  const [article, setArticle] = useState<Article | null>(null);
  const [category, setCategory] = useState<Category | null>(null);
  const [author, setAuthor] = useState<Author | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [isThumbsUpActive, setIsThumbsUpActive] = useState(false);
  const [isFavoriteActive, setIsFavoriteActive] = useState(false);
  const [isFollowing, setIsFollowing] = useState(false);
  const [thumbsUpLoading, setThumbsUpLoading] = useState(false);
  const [favoriteLoading, setFavoriteLoading] = useState(false);
  const [followLoading, setFollowLoading] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const articleId = id ?? '';

  const fetchArticle = useCallback(async (showLoading = false) => {
    if (!articleId) return;
    if (showLoading) {
      setLoading(true);
    }
    try {
      const res = await articleQueryOne(articleId);
      const art = res.data.data!;
      setArticle(art);

      const [catRes, commentRes] = await Promise.all([
        categoryQueryOne(art.categoryId),
        commentGetList(art.id),
      ]);
      setCategory(catRes.data.data ?? null);
      setComments(commentRes.data.data ?? []);

      try {
        const authorRes = await authorQueryOneById(art.authorId);
        setAuthor(authorRes.data.data ?? null);
      } catch {
        setAuthor(null);
      }
    } catch (err: unknown) {
      error(getErrorMessage(err, '加载文章失败'));
      navigate(ROUTES.HOME);
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [articleId, error, navigate]);

  useEffect(() => {
    fetchArticle(true);
  }, [fetchArticle]);

  const fetchInteractionState = useCallback(async () => {
    if (!isAuthenticated || !user?.id || !articleId) {
      setIsThumbsUpActive(false);
      setIsFavoriteActive(false);
      return;
    }

    try {
      const [thumbsUpRes, favoriteRes] = await Promise.all([
        thumbsUpMyList(),
        favoriteMyList(),
      ]);

      const thumbsUpIds = thumbsUpRes.data.data ?? [];
      const favoriteIds = favoriteRes.data.data ?? [];

      setIsThumbsUpActive(thumbsUpIds.includes(articleId));
      setIsFavoriteActive(favoriteIds.includes(articleId));
    } catch {
      setIsThumbsUpActive(false);
      setIsFavoriteActive(false);
    }
  }, [articleId, isAuthenticated, user?.id]);

  useEffect(() => {
    fetchInteractionState();
  }, [fetchInteractionState]);

  useEffect(() => {
    if (!isAuthenticated || !user?.id || !article?.authorId || user.id === article.authorId) {
      setIsFollowing(false);
      return;
    }

    followsCheckOne({ authorId: user.id, targetId: article.authorId })
      .then((response) => {
        setIsFollowing(!!response.data.data);
      })
      .catch(() => {
        setIsFollowing(false);
      });
  }, [article?.authorId, isAuthenticated, user?.id]);

  const handleThumbsUp = async () => {
    if (!isAuthenticated) {
      error('请先登录');
      return;
    }
    if (interactionLoading) return;

    setThumbsUpLoading(true);
    try {
      await thumbsUpToggle(articleId);
      await Promise.all([fetchArticle(false), fetchInteractionState()]);
      success('操作成功');
    } catch (err: unknown) {
      error(getErrorMessage(err, '操作失败'));
    } finally {
      setThumbsUpLoading(false);
    }
  };

  const handleFavorite = async () => {
    if (!isAuthenticated) {
      error('请先登录');
      return;
    }
    if (interactionLoading) return;

    setFavoriteLoading(true);
    try {
      await favoriteToggle(articleId);
      await Promise.all([fetchArticle(false), fetchInteractionState()]);
      success('操作成功');
    } catch (err: unknown) {
      error(getErrorMessage(err, '操作失败'));
    } finally {
      setFavoriteLoading(false);
    }
  };

  const handleFollow = async () => {
    if (!user?.id || !article?.authorId || user.id === article.authorId || followLoading) return;

    setFollowLoading(true);
    try {
      if (isFollowing) {
        await followsUnfollow({ authorId: user.id, targetId: article.authorId });
        setIsFollowing(false);
        success('已取消关注');
      } else {
        await followsFollow({ authorId: user.id, targetId: article.authorId });
        setIsFollowing(true);
        success('关注成功');
      }
    } catch (err: unknown) {
      error(getErrorMessage(err, '操作失败'));
    } finally {
      setFollowLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!article) return;
    setDeleting(true);
    try {
      await articleDelete({ id: article.id, authorId: article.authorId });
      success('文章已删除');
      navigate(ROUTES.HOME);
    } catch (err: unknown) {
      error(getErrorMessage(err, '删除失败'));
    } finally {
      setDeleting(false);
      setDeleteModalOpen(false);
    }
  };

  const isOwner = !!user && !!article && user.id === article.authorId;
  const canFollowAuthor = isAuthenticated && !!user?.id && !!article?.authorId && !isOwner;
  const interactionLoading = thumbsUpLoading || favoriteLoading;

  if (loading) return <PageLayout><Loading fullscreen /></PageLayout>;
  if (!article) return null;

  const articleHtml = getSafeRichTextHtml(article.content);

  return (
    <PageLayout>
      <Container>
        <div className={styles.layout}>
          <article className={styles.article}>
            <nav className={styles.breadcrumb}>
              <Link to="/" className={styles['breadcrumb-link']}>首页</Link>
              <span className={styles['breadcrumb-sep']}>/</span>
              {category && (
                <>
                  <Link to={ROUTES.CATEGORY.replace(':id', String(category.id))} className={styles['breadcrumb-link']}>{category.name}</Link>
                  <span className={styles['breadcrumb-sep']}>/</span>
                </>
              )}
              <span className={styles['breadcrumb-current']}>{article.title}</span>
            </nav>

            <header className={styles.header}>
              {category && (
                <Link to={ROUTES.CATEGORY.replace(':id', String(category.id))} className={styles['category-tag']}>
                  {category.name}
                </Link>
              )}
              <h1 className={styles.title}>{article.title}</h1>
              <div className={styles.meta}>
                <div className={styles['meta-author']}>
                  <Link to={ROUTES.AUTHOR_DETAIL.replace(':id', String(article.authorId))} className={styles['author-link']}>
                    <div className={styles['author-avatar']}>
                      {(author?.username ?? 'A')[0].toUpperCase()}
                    </div>
                    <span>{author?.username ?? `用户${article.authorId}`}</span>
                  </Link>
                  {canFollowAuthor && (
                    <Button
                      type="button"
                      variant={isFollowing ? 'outline' : 'primary'}
                      size="small"
                      loading={followLoading}
                      onClick={handleFollow}
                      className={styles['follow-btn']}
                    >
                      {isFollowing ? '取消关注' : '关注作者'}
                    </Button>
                  )}
                </div>
                <time className={styles['meta-date']}>{formatDate(article.createTime)}</time>
                {article.updateTime !== article.createTime && (
                  <span className={styles['meta-updated']}>更新于 {formatDate(article.updateTime)}</span>
                )}
              </div>
            </header>

            <div
              className={styles.body}
              dangerouslySetInnerHTML={{ __html: articleHtml }}
            />

            <div className={styles.actions}>
              <button
                type="button"
                className={`${styles['action-btn']} ${isThumbsUpActive ? styles['action-btn-active'] : ''}`}
                onClick={handleThumbsUp}
                disabled={interactionLoading}
                aria-busy={thumbsUpLoading}
                aria-pressed={isThumbsUpActive}
              >
                <span>{thumbsUpLoading ? '···' : '♥'}</span> {article.thumbsUp}
              </button>
              <button
                type="button"
                className={`${styles['action-btn']} ${isFavoriteActive ? styles['action-btn-active'] : ''}`}
                onClick={handleFavorite}
                disabled={interactionLoading}
                aria-busy={favoriteLoading}
                aria-pressed={isFavoriteActive}
              >
                <span>{favoriteLoading ? '···' : '★'}</span> {article.favorites}
              </button>
              {isOwner && (
                <div className={styles['owner-actions']}>
                  <Button variant="outline" size="small" onClick={() => navigate(ROUTES.ARTICLE_EDIT.replace(':id', String(article.id)))}>
                    编辑
                  </Button>
                  <Button variant="outline" size="small" onClick={() => setDeleteModalOpen(true)}>
                    删除
                  </Button>
                </div>
              )}
            </div>

            <CommentSection
              articleId={articleId}
              comments={comments}
              onCommentAdded={fetchArticle}
            />
          </article>
        </div>
      </Container>

      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="确认删除"
        footer={(
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <Button variant="outline" onClick={() => setDeleteModalOpen(false)}>取消</Button>
            <Button variant="primary" loading={deleting} onClick={handleDelete}>确认删除</Button>
          </div>
        )}
      >
        <p>确定要删除这篇文章吗？此操作不可撤销。</p>
      </Modal>
    </PageLayout>
  );
};

export default ArticleDetail;
