import type { Id } from '../types';
import { STORAGE_KEYS, USER_ROLE, type UserRole } from './constants';

/**
 * Token management helpers.
 */

const TOKEN_KEY = STORAGE_KEYS.TOKEN;
const USER_KEY = STORAGE_KEYS.USER_INFO;

export interface UserInfo {
  id: Id;
  username: string;
  role: UserRole;
}

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
};

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY);
};

export const hasToken = (): boolean => {
  return !!getToken();
};

export const getUserInfo = (): UserInfo | null => {
  const userStr = localStorage.getItem(USER_KEY);
  if (!userStr) return null;

  try {
    const parsed = JSON.parse(userStr) as Partial<UserInfo>;
    if (!parsed.id || !parsed.username || !parsed.role) {
      return null;
    }

    return {
      id: String(parsed.id),
      username: parsed.username,
      role: parsed.role,
    };
  } catch {
    return null;
  }
};

export const setUserInfo = (user: UserInfo): void => {
  localStorage.setItem(USER_KEY, JSON.stringify({
    ...user,
    id: String(user.id),
  }));
};

export const removeUserInfo = (): void => {
  localStorage.removeItem(USER_KEY);
};

export const clearAuth = (): void => {
  removeToken();
  removeUserInfo();
};

export const isAdmin = (): boolean => {
  const user = getUserInfo();
  return user?.role === USER_ROLE.ADMIN;
};

export const isAuthenticated = (): boolean => {
  return hasToken() && !!getUserInfo();
};
