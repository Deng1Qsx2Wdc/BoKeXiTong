import React, { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { TOAST_TYPE, TOAST_DURATION } from '../utils/constants';

export type ToastType = typeof TOAST_TYPE[keyof typeof TOAST_TYPE];

export interface Toast {
  id: string;
  type: ToastType;
  message: string;
  duration?: number;
}

interface ToastContextType {
  toasts: Toast[];
  showToast: (message: string, type?: ToastType, duration?: number) => void;
  removeToast: (id: string) => void;
  success: (message: string, duration?: number) => void;
  error: (message: string, duration?: number) => void;
  warning: (message: string, duration?: number) => void;
  info: (message: string, duration?: number) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback(
    (message: string, type: ToastType = TOAST_TYPE.INFO, duration: number = TOAST_DURATION.MEDIUM) => {
      // Deduplicate: don't add if same message+type already visible
      setToasts((prev) => {
        if (prev.some((t) => t.message === message && t.type === type)) return prev;
        const id = `toast-${Date.now()}-${Math.random()}`;
        const toast: Toast = { id, type, message, duration };
        if (duration > 0) {
          setTimeout(() => removeToast(id), duration);
        }
        return [...prev, toast];
      });
    },
    [removeToast]
  );

  const success = useCallback(
    (message: string, duration?: number) => {
      showToast(message, TOAST_TYPE.SUCCESS, duration);
    },
    [showToast]
  );

  const error = useCallback(
    (message: string, duration?: number) => {
      showToast(message, TOAST_TYPE.ERROR, duration);
    },
    [showToast]
  );

  const warning = useCallback(
    (message: string, duration?: number) => {
      showToast(message, TOAST_TYPE.WARNING, duration);
    },
    [showToast]
  );

  const info = useCallback(
    (message: string, duration?: number) => {
      showToast(message, TOAST_TYPE.INFO, duration);
    },
    [showToast]
  );

  const value: ToastContextType = {
    toasts,
    showToast,
    removeToast,
    success,
    error,
    warning,
    info,
  };

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>;
};

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within ToastProvider');
  }
  return context;
};
