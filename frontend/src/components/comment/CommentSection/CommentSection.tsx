import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { authorQueryOneById } from '../../../api/author';
import { getErrorMessage } from '../../../api/client';
import { commentSet } from '../../../api/comment';
import { useAuth } from '../../../context/AuthContext';
import { useToast } from '../../../context/ToastContext';
import { ROUTES } from '../../../utils/constants';
import { formatDate } from '../../../utils/format';
import type { Comment, Id } from '../../../types';
import { Button } from '../../common/Button';
import styles from './CommentSection.module.css';

interface CommentSectionProps {
  articleId: Id;
  comments: Comment[];
  onCommentAdded: () => void;
}

const collectAuthorIds = (commentList: Comment[]): Id[] => {
  const ids = new Set<Id>();

  const walk = (items: Comment[]) => {
    items.forEach((item) => {
      ids.add(item.authorId);
      if (item.children?.length) {
        walk(item.children);
      }
    });
  };

  walk(commentList);
  return [...ids];
};

export const CommentSection: React.FC<CommentSectionProps> = ({
  articleId,
  comments,
  onCommentAdded,
}) => {
  const { isAuthenticated } = useAuth();
  const { success, error } = useToast();
  const [content, setContent] = useState('');
  const [replyTo, setReplyTo] = useState<Id | undefined>(undefined);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [authorMap, setAuthorMap] = useState<Record<Id, string>>({});

  useEffect(() => {
    const ids = collectAuthorIds(comments);
    const missingIds = ids.filter((id) => !authorMap[id]);
    if (missingIds.length === 0) return;

    Promise.allSettled(
      missingIds.map(async (id) => {
        const res = await authorQueryOneById(id);
        return [id, res.data.data?.username ?? `用户${id}`] as const;
      }),
    ).then((results) => {
      const entries = results
        .filter((result): result is PromiseFulfilledResult<readonly [Id, string]> => result.status === 'fulfilled')
        .map((result) => result.value);

      if (entries.length === 0) return;
      setAuthorMap((prev) => ({
        ...prev,
        ...Object.fromEntries(entries),
      }));
    });
  }, [authorMap, comments]);

  const handleSubmit = async (parentId?: Id) => {
    const text = parentId !== undefined ? replyContent : content;
    if (!text.trim()) {
      error('评论内容不能为空');
      return;
    }

    setSubmitting(true);
    try {
      await commentSet({
        articleId,
        parentId,
        content: text.trim(),
      });
      success('评论成功');
      if (parentId !== undefined) {
        setReplyContent('');
        setReplyTo(undefined);
      } else {
        setContent('');
      }
      onCommentAdded();
    } catch (err: unknown) {
      error(getErrorMessage(err, '评论失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const renderComment = (comment: Comment, depth = 0) => {
    const authorName = authorMap[comment.authorId] ?? `用户${comment.authorId}`;

    return (
      <div key={comment.id} className={`${styles.comment} ${depth > 0 ? styles['comment-reply'] : ''}`}>
        <div className={styles['comment-avatar']}>
          {authorName[0]?.toUpperCase() ?? 'U'}
        </div>
        <div className={styles['comment-body']}>
          <div className={styles['comment-meta']}>
            <span className={styles['comment-author']}>{authorName}</span>
            <time className={styles['comment-time']}>{formatDate(comment.createTime)}</time>
          </div>
          <p className={styles['comment-content']}>{comment.content}</p>
          {isAuthenticated && depth === 0 && (
            <button
              className={styles['reply-btn']}
              onClick={() => setReplyTo(replyTo === comment.id ? undefined : comment.id)}
            >
              回复
            </button>
          )}
          {replyTo === comment.id && (
            <div className={styles['reply-form']}>
              <textarea
                className={styles.textarea}
                placeholder="写下你的回复"
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                rows={3}
              />
              <div className={styles['form-actions']}>
                <Button variant="ghost" size="small" onClick={() => setReplyTo(undefined)} type="button">取消</Button>
                <Button size="small" loading={submitting} onClick={() => handleSubmit(comment.id)} type="button">回复</Button>
              </div>
            </div>
          )}
          {comment.children?.map((child) => renderComment(child, depth + 1))}
        </div>
      </div>
    );
  };

  return (
    <section className={styles.section}>
      <h3 className={styles.title}>评论 ({comments.length})</h3>

      {isAuthenticated ? (
        <div className={styles.form}>
          <textarea
            className={styles.textarea}
            placeholder="写下你的评论"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={4}
          />
          <div className={styles['form-actions']}>
            <Button loading={submitting} onClick={() => handleSubmit(undefined)} type="button">发表评论</Button>
          </div>
        </div>
      ) : (
        <p className={styles['login-hint']}>
          <Link to={ROUTES.LOGIN}>登录</Link> 后参与评论
        </p>
      )}

      <div className={styles.list}>
        {comments.length === 0 ? (
          <p className={styles.empty}>暂无评论，来发表第一条评论吧</p>
        ) : (
          comments.map((comment) => renderComment(comment))
        )}
      </div>
    </section>
  );
};
