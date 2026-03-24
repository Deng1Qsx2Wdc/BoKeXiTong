import React from 'react';
import { Link } from 'react-router-dom';
import type { Article, Category } from '../../../types';
import { formatDate, truncateText } from '../../../utils/format';
import { ARTICLE_STATUS, ROUTES } from '../../../utils/constants';
import { richTextToPlainText } from '../../../utils/richText';
import styles from './ArticleCard.module.css';

interface ArticleCardProps {
  article: Article;
  category?: Category;
  authorName?: string;
  index?: number;
}

export const ArticleCard: React.FC<ArticleCardProps> = ({ article, category, authorName, index = 0 }) => {
  const animationDelay = `${index * 80}ms`;

  return (
    <article
      className={styles.card}
      style={{ animationDelay }}
    >
      <div className={styles['card-meta']}>
        {category && (
          <Link to={ROUTES.CATEGORY.replace(':id', String(category.id))} className={styles['card-category']}>
            {category.name}
          </Link>
        )}
        <time className={styles['card-date']}>{formatDate(article.createTime)}</time>
      </div>

      <h2 className={styles['card-title']}>
        <Link to={ROUTES.ARTICLE_DETAIL.replace(':id', String(article.id))} className={styles['card-title-link']}>
          {article.title}
        </Link>
      </h2>

      <p className={styles['card-excerpt']}>
        {truncateText(richTextToPlainText(article.content), 120)}
      </p>

      <div className={styles['card-footer']}>
        <div className={styles['card-author']}>
          <div className={styles['author-avatar']}>
            {(authorName ?? 'A')[0].toUpperCase()}
          </div>
          <span className={styles['author-name']}>{authorName ?? '匿名'}</span>
        </div>
        <div className={styles['card-stats']}>
          <span className={styles['card-stat']}>♥ {article.thumbsUp}</span>
          <span className={styles['card-stat']}>★ {article.favorites}</span>
          {article.status === ARTICLE_STATUS.DRAFT && (
            <span className={styles['card-draft']}>草稿</span>
          )}
        </div>
      </div>
    </article>
  );
};
