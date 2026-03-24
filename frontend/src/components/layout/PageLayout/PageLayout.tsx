import React from 'react';
import { Header } from '../Header/Header';
import { Footer } from '../Footer/Footer';
import styles from './PageLayout.module.css';

interface PageLayoutProps {
  children: React.ReactNode;
  /** hide header/footer for fullscreen pages like login */
  bare?: boolean;
}

export const PageLayout: React.FC<PageLayoutProps> = ({ children, bare = false }) => {
  if (bare) return <>{children}</>;
  return (
    <div className={styles.layout}>
      <Header />
      <main className={styles.main}>{children}</main>
      <Footer />
    </div>
  );
};
