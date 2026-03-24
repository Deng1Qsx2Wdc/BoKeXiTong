/**
 * 表单验证工具
 */

export interface ValidationResult {
  valid: boolean;
  message?: string;
}

/**
 * 验证用户名
 */
export const validateUsername = (username: string): ValidationResult => {
  if (!username) {
    return { valid: false, message: '用户名不能为空' };
  }

  if (username.length < 3) {
    return { valid: false, message: '用户名至少3个字符' };
  }

  if (username.length > 20) {
    return { valid: false, message: '用户名最多20个字符' };
  }

  if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(username)) {
    return { valid: false, message: '用户名只能包含字母、数字、下划线和中文' };
  }

  return { valid: true };
};

/**
 * 验证密码
 */
export const validatePassword = (password: string): ValidationResult => {
  if (!password) {
    return { valid: false, message: '密码不能为空' };
  }

  if (password.length < 6) {
    return { valid: false, message: '密码至少6个字符' };
  }

  if (password.length > 20) {
    return { valid: false, message: '密码最多20个字符' };
  }

  return { valid: true };
};

/**
 * 验证确认密码
 */
export const validateConfirmPassword = (password: string, confirmPassword: string): ValidationResult => {
  if (!confirmPassword) {
    return { valid: false, message: '请确认密码' };
  }

  if (password !== confirmPassword) {
    return { valid: false, message: '两次密码输入不一致' };
  }

  return { valid: true };
};

/**
 * 验证邮箱
 */
export const validateEmail = (email: string): ValidationResult => {
  if (!email) {
    return { valid: false, message: '邮箱不能为空' };
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return { valid: false, message: '邮箱格式不正确' };
  }

  return { valid: true };
};

/**
 * 验证文章标题
 */
export const validateArticleTitle = (title: string): ValidationResult => {
  if (!title) {
    return { valid: false, message: '标题不能为空' };
  }

  if (title.length < 2) {
    return { valid: false, message: '标题至少2个字符' };
  }

  if (title.length > 100) {
    return { valid: false, message: '标题最多100个字符' };
  }

  return { valid: true };
};

/**
 * 验证文章内容
 */
export const validateArticleContent = (content: string): ValidationResult => {
  if (!content) {
    return { valid: false, message: '内容不能为空' };
  }

  if (content.length < 10) {
    return { valid: false, message: '内容至少10个字符' };
  }

  return { valid: true };
};

/**
 * 验证评论内容
 */
export const validateCommentContent = (content: string): ValidationResult => {
  if (!content) {
    return { valid: false, message: '评论不能为空' };
  }

  if (content.length < 2) {
    return { valid: false, message: '评论至少2个字符' };
  }

  if (content.length > 500) {
    return { valid: false, message: '评论最多500个字符' };
  }

  return { valid: true };
};

/**
 * 验证分类名称
 */
export const validateCategoryName = (name: string): ValidationResult => {
  if (!name) {
    return { valid: false, message: '分类名称不能为空' };
  }

  if (name.length < 2) {
    return { valid: false, message: '分类名称至少2个字符' };
  }

  if (name.length > 20) {
    return { valid: false, message: '分类名称最多20个字符' };
  }

  return { valid: true };
};
