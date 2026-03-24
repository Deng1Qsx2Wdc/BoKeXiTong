import React from 'react';
import { useToast } from '../../../context/ToastContext';
import { TOAST_TYPE } from '../../../utils/constants';
import styles from './Toast.module.css';

export const ToastContainer: React.FC = () => {
  const { toasts, removeToast } = useToast();

  const getIcon = (type: string) => {
    switch (type) {
      case TOAST_TYPE.SUCCESS:
        return '✓';
      case TOAST_TYPE.ERROR:
        return '✕';
      case TOAST_TYPE.WARNING:
        return '!';
      case TOAST_TYPE.INFO:
      default:
        return 'i';
    }
  };

  return (
    <div className={styles['toast-container']}>
      {toasts.map((toast) => (
        <div key={toast.id} className={`${styles.toast} ${styles[`toast-${toast.type}`]}`}>
          <div className={styles['toast-icon']}>{getIcon(toast.type)}</div>
          <div className={styles['toast-content']}>{toast.message}</div>
          <button className={styles['toast-close']} onClick={() => removeToast(toast.id)} aria-label="关闭">
            ✕
          </button>
        </div>
      ))}
    </div>
  );
};
