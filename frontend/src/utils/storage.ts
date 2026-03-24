/**
 * 本地存储封装
 */

/**
 * 设置本地存储
 */
export const setStorage = <T>(key: string, value: T): void => {
  try {
    const serializedValue = JSON.stringify(value);
    localStorage.setItem(key, serializedValue);
  } catch (error) {
    console.error('Failed to set storage:', error);
  }
};

/**
 * 获取本地存储
 */
export const getStorage = <T>(key: string): T | null => {
  try {
    const item = localStorage.getItem(key);
    if (!item) return null;
    return JSON.parse(item) as T;
  } catch (error) {
    console.error('Failed to get storage:', error);
    return null;
  }
};

/**
 * 移除本地存储
 */
export const removeStorage = (key: string): void => {
  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.error('Failed to remove storage:', error);
  }
};

/**
 * 清空本地存储
 */
export const clearStorage = (): void => {
  try {
    localStorage.clear();
  } catch (error) {
    console.error('Failed to clear storage:', error);
  }
};

/**
 * 设置会话存储
 */
export const setSessionStorage = <T>(key: string, value: T): void => {
  try {
    const serializedValue = JSON.stringify(value);
    sessionStorage.setItem(key, serializedValue);
  } catch (error) {
    console.error('Failed to set session storage:', error);
  }
};

/**
 * 获取会话存储
 */
export const getSessionStorage = <T>(key: string): T | null => {
  try {
    const item = sessionStorage.getItem(key);
    if (!item) return null;
    return JSON.parse(item) as T;
  } catch (error) {
    console.error('Failed to get session storage:', error);
    return null;
  }
};

/**
 * 移除会话存储
 */
export const removeSessionStorage = (key: string): void => {
  try {
    sessionStorage.removeItem(key);
  } catch (error) {
    console.error('Failed to remove session storage:', error);
  }
};

/**
 * 清空会话存储
 */
export const clearSessionStorage = (): void => {
  try {
    sessionStorage.clear();
  } catch (error) {
    console.error('Failed to clear session storage:', error);
  }
};
