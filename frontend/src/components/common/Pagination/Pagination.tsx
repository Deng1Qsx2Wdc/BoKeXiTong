import React from 'react';
import { PAGINATION } from '../../../utils/constants';
import styles from './Pagination.module.css';

interface PaginationProps {
  current: number;
  total: number;
  pageSize?: number;
  onChange: (page: number) => void;
}

export const Pagination: React.FC<PaginationProps> = ({ current, total, pageSize = PAGINATION.DEFAULT_PAGE_SIZE, onChange }) => {
  const totalPages = Math.ceil(total / pageSize);
  if (totalPages <= 1) return null;

  const pages: (number | '...')[] = [];
  if (totalPages <= 7) {
    for (let i = 1; i <= totalPages; i++) pages.push(i);
  } else {
    pages.push(1);
    if (current > 4) pages.push('...');
    const start = Math.max(2, current - 1);
    const end = Math.min(totalPages - 1, current + 1);
    for (let i = start; i <= end; i++) pages.push(i);
    if (current < totalPages - 3) pages.push('...');
    pages.push(totalPages);
  }

  return (
    <nav className={styles.pagination} aria-label="分页">
      <button
        className={`${styles['page-btn']} ${styles['page-nav']}`}
        disabled={current === 1}
        onClick={() => onChange(current - 1)}
      >
        ←
      </button>
      {pages.map((p, i) =>
        p === '...' ? (
          <span key={`ellipsis-${i}`} className={styles['page-ellipsis']}>…</span>
        ) : (
          <button
            key={p}
            className={`${styles['page-btn']} ${p === current ? styles['page-btn-active'] : ''}`}
            onClick={() => onChange(p as number)}
          >
            {p}
          </button>
        )
      )}
      <button
        className={`${styles['page-btn']} ${styles['page-nav']}`}
        disabled={current === totalPages}
        onClick={() => onChange(current + 1)}
      >
        →
      </button>
    </nav>
  );
};
