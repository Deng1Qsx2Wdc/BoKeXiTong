import React from 'react';
import styles from './Loading.module.css';

export interface LoadingProps {
  fullscreen?: boolean;
  size?: 'small' | 'medium';
}

export const Loading: React.FC<LoadingProps> = ({ fullscreen = false, size = 'medium' }) => {
  if (fullscreen) {
    return (
      <div className={styles['loading-overlay']}>
        <div className={styles['loading-spinner']} />
      </div>
    );
  }

  return (
    <div className={styles['loading-inline']}>
      <div className={`${styles['loading-spinner']} ${size === 'small' ? styles['loading-spinner-small'] : ''}`} />
    </div>
  );
};
