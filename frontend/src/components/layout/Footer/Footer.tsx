import React from 'react';
import { Link } from 'react-router-dom';
import { ROUTES } from '../../../utils/constants';
import styles from './Footer.module.css';

export const Footer: React.FC = () => {
  const year = new Date().getFullYear();

  return (
    <footer className={styles.footer}>
      <div className={styles['footer-inner']}>
        <div className={styles['footer-brand']}>
          <span className={styles['footer-logo']}>博文</span>
          <p className={styles['footer-tagline']}>记录思想，分享文字</p>
        </div>
        <nav className={styles['footer-nav']}>
          <Link to={ROUTES.HOME} className={styles['footer-link']}>首页</Link>
          <Link to={ROUTES.REGISTER} className={styles['footer-link']}>注册</Link>
          <Link to={ROUTES.LOGIN} className={styles['footer-link']}>登录</Link>
        </nav>
        <p className={styles['footer-copy']}>© {year} 博文. All rights reserved.</p>
      </div>
    </footer>
  );
};
