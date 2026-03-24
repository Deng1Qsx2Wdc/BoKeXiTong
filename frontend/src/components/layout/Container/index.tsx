import React from 'react';
import styles from './Container.module.css';

interface ContainerProps {
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'full';
  className?: string;
}

export const Container: React.FC<ContainerProps> = ({ children, size = 'lg', className = '' }) => {
  return (
    <div className={`${styles.container} ${styles[`container-${size}`]} ${className}`}>
      {children}
    </div>
  );
};
