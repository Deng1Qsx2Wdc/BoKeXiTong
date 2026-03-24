import React from 'react';
import { Link } from 'react-router-dom';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import styles from './NotFound.module.css';

const NotFound: React.FC = () => (
  <PageLayout>
    <div className={styles.wrap}>
      <p className={styles.code}>404</p>
      <h1 className={styles.title}>页面不存在</h1>
      <p className={styles.desc}>你访问的页面可能已被移除或输入了错误的地址。</p>
      <Link to="/" className={styles.btn}>返回首页</Link>
    </div>
  </PageLayout>
);

export default NotFound;
