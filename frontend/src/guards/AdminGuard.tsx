import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROUTES } from '../utils/constants';

interface AdminGuardProps {
  children: React.ReactNode;
}

/**
 * 管理员守卫 - 保护需要管理员权限的路由
 */
export const AdminGuard: React.FC<AdminGuardProps> = ({ children }) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    // 未登录，跳转到管理员登录页
    return <Navigate to={ROUTES.ADMIN_LOGIN} replace />;
  }

  if (!isAdmin) {
    // 已登录但不是管理员，跳转到首页
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <>{children}</>;
};
