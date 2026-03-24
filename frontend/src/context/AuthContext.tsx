import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { type UserInfo, getToken, setToken, getUserInfo, setUserInfo, clearAuth } from '../utils/token';
import { USER_ROLE } from '../utils/constants';

interface AuthContextType {
  user: UserInfo | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (token: string, user: UserInfo) => void;
  logout: () => void;
  updateUser: (user: UserInfo) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [token, setTokenState] = useState<string | null>(null);

  useEffect(() => {
    // 初始化时从 localStorage 读取认证信息
    const storedToken = getToken();
    const storedUser = getUserInfo();

    if (storedToken && storedUser) {
      setTokenState(storedToken);
      setUser(storedUser);
    }
  }, []);

  const login = (newToken: string, newUser: UserInfo) => {
    setToken(newToken);
    setUserInfo(newUser);
    setTokenState(newToken);
    setUser(newUser);
  };

  const logout = () => {
    clearAuth();
    setTokenState(null);
    setUser(null);
  };

  const updateUser = (newUser: UserInfo) => {
    setUserInfo(newUser);
    setUser(newUser);
  };

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isAdmin: user?.role === USER_ROLE.ADMIN,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
