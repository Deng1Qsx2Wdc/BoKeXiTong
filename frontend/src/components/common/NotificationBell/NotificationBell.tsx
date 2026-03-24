import React, { useState, useCallback } from 'react';
import { useWebSocket } from '../../../hooks/useWebSocket';
import { useAuth } from '../../../context/AuthContext';
import styles from './NotificationBell.module.css';

interface Notification {
  id: string;
  message: string;
  time: Date;
  read: boolean;
}

const getNotificationMessage = (data: unknown) => {
  if (typeof data === 'string') {
    return data;
  }

  if (
    typeof data === 'object'
    && data !== null
    && 'message' in data
    && typeof data.message === 'string'
  ) {
    return data.message;
  }

  try {
    return JSON.stringify(data) ?? '收到新通知';
  } catch {
    return '收到新通知';
  }
};

export const NotificationBell: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [open, setOpen] = useState(false);

  const handleMessage = useCallback((data: unknown) => {
    setNotifications((prev) => [
      {
        id: Date.now().toString(),
        message: getNotificationMessage(data),
        time: new Date(),
        read: false,
      },
      ...prev.slice(0, 19),
    ]);
  }, []);

  useWebSocket({ onMessage: handleMessage, enabled: isAuthenticated });

  const unread = notifications.filter((n) => !n.read).length;

  const markAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  if (!isAuthenticated) return null;

  return (
    <div className={styles.wrap}>
      <button
        className={styles.bell}
        onClick={() => { setOpen((o) => !o); if (!open) markAllRead(); }}
        aria-label="通知"
      >
        <span className={styles['bell-icon']}>🔔</span>
        {unread > 0 && <span className={styles.badge}>{unread > 9 ? '9+' : unread}</span>}
      </button>

      {open && (
        <div className={styles.dropdown}>
          <div className={styles['dropdown-header']}>
            <span>通知</span>
            {notifications.length > 0 && (
              <button className={styles['clear-btn']} onClick={() => setNotifications([])}>
                清空
              </button>
            )}
          </div>
          {notifications.length === 0 ? (
            <p className={styles.empty}>暂无通知</p>
          ) : (
            <ul className={styles.list}>
              {notifications.map((n) => (
                <li key={n.id} className={styles.item}>
                  <p className={styles['item-msg']}>{n.message}</p>
                  <time className={styles['item-time']}>
                    {n.time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
                  </time>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};
