import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../../../utils/constants';
import styles from './SearchBar.module.css';

export const SearchBar: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [keyword, setKeyword] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const q = keyword.trim();
    if (!q) return;
    navigate(`${ROUTES.HOME}?keyword=${encodeURIComponent(q)}`);
    setOpen(false);
    setKeyword('');
  };

  return (
    <div className={styles.wrap}>
      {open ? (
        <form onSubmit={handleSearch} className={styles.form}>
          <input
            ref={inputRef}
            className={styles.input}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="搜标题 / 正文 / 文章ID / 作者ID"
          />
          <button type="submit" className={styles.submit}>搜索</button>
          <button type="button" className={styles.cancel} onClick={() => setOpen(false)}>✕</button>
        </form>
      ) : (
        <button className={styles.icon} onClick={() => setOpen(true)} aria-label="搜索">
          &#x2315;
        </button>
      )}
    </div>
  );
};
