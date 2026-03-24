/**
 * Shared frontend constants.
 */

const DEFAULT_API_BASE_URL = 'http://localhost:8080';
const DEFAULT_WS_PATH = '/ws';

const normalizeUrl = (value: string) => value.replace(/\/$/, '');

const buildDefaultWsBaseUrl = (apiBaseUrl: string) => {
  try {
    const parsedUrl = new URL(apiBaseUrl);
    const protocol = parsedUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${parsedUrl.host}${DEFAULT_WS_PATH}`;
  } catch {
    return `ws://localhost:8080${DEFAULT_WS_PATH}`;
  }
};

export const API_BASE_URL = normalizeUrl(import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL);
export const WS_BASE_URL = normalizeUrl(import.meta.env.VITE_WS_BASE_URL || buildDefaultWsBaseUrl(API_BASE_URL));

export const API_TIMEOUT = 30_000;
export const WEBSOCKET_RECONNECT_DELAY = 5_000;

export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_PAGE_SIZE: 10,
  ADMIN_PAGE_SIZE: 15,
  LARGE_PAGE_SIZE: 100,
  AUTHOR_PAGE_SIZE: 20,
  CATEGORY_OPTIONS_PAGE_SIZE: 200,
  PAGE_SIZE_OPTIONS: [10, 20, 30, 50],
} as const;

export const ARTICLE_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  OFFLINE: 2,
} as const;

export const ARTICLE_STATUS_TEXT: Record<number, string> = {
  [ARTICLE_STATUS.DRAFT]: '草稿',
  [ARTICLE_STATUS.PUBLISHED]: '已发布',
  [ARTICLE_STATUS.OFFLINE]: '已下架',
};

export const USER_ROLE = {
  USER: 'USER',
  ADMIN: 'ADMIN',
} as const;

export type UserRole = typeof USER_ROLE[keyof typeof USER_ROLE];

export const USER_ROLE_TEXT: Record<UserRole, string> = {
  [USER_ROLE.USER]: '普通用户',
  [USER_ROLE.ADMIN]: '管理员',
};

export const TOAST_TYPE = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info',
} as const;

export const TOAST_DURATION = {
  SHORT: 2000,
  MEDIUM: 3000,
  LONG: 5000,
} as const;

export const DEBOUNCE_DELAY = {
  SEARCH: 500,
  INPUT: 300,
  RESIZE: 200,
} as const;

export const FILE_UPLOAD = {
  MAX_SIZE: 5 * 1024 * 1024,
  ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
} as const;

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  ARTICLE_DETAIL: '/article/:id',
  ARTICLE_NEW: '/article/new',
  ARTICLE_EDIT: '/article/edit/:id',
  CATEGORY: '/category/:id',
  AUTHOR_DETAIL: '/author/:id',
  PROFILE: '/profile',
  PROFILE_ARTICLES: '/profile/articles',
  PROFILE_FAVORITES: '/profile/favorites',
  PROFILE_FOLLOWS: '/profile/follows',
  PROFILE_SETTINGS: '/profile/settings',
  ADMIN_PREFIX: '/admin',
  ADMIN_LOGIN: '/admin/login',
  ADMIN_DASHBOARD: '/admin/dashboard',
  ADMIN_USERS: '/admin/users',
  ADMIN_CATEGORIES: '/admin/categories',
  ADMIN_ARTICLES: '/admin/articles',
  ADMIN_ARTICLE_EDIT: '/admin/articles/edit/:id',
} as const;

export const STORAGE_KEYS = {
  TOKEN: 'auth_token',
  USER_INFO: 'user_info',
  THEME: 'theme',
  DRAFT: 'article_draft',
} as const;
