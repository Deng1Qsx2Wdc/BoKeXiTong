import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { adminLogin } from '../api/admin';
import { getErrorMessage } from '../api/client';
import { authorLogin } from '../api/author';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import type { AdminLoginResponse, AuthorLoginResponse, Login as LoginData } from '../types';
import { ROUTES, USER_ROLE } from '../utils/constants';
import { validatePassword, validateUsername } from '../utils/validation';
import styles from './Login.module.css';

type LoginType = 'user' | 'admin';
type LoginErrors = Partial<Record<keyof LoginData, string>>;
type LoginLocationState = {
  from?: {
    pathname?: string;
  };
};

const LOGIN_META: Record<LoginType, { title: string; subtitle: string; submitText: string }> = {
  user: {
    title: '欢迎回来',
    subtitle: '使用你的用户账号继续写作、收藏和互动',
    submitText: '登录',
  },
  admin: {
    title: '管理后台登录',
    subtitle: '使用管理员账号进入站点运营控制台',
    submitText: '进入后台',
  },
};

const parseUserIdFromToken = (token: string) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return String(payload?.sub ?? '');
  } catch {
    return '';
  }
};

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as LoginLocationState | null)?.from?.pathname ?? ROUTES.HOME;
  const { login } = useAuth();
  const { success, error: showError } = useToast();

  const [loginType, setLoginType] = useState<LoginType>('user');
  const [formData, setFormData] = useState<LoginData>({
    name: '',
    password: '',
  });
  const [errors, setErrors] = useState<LoginErrors>({});
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleInputChange = (field: keyof LoginData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setFormData((prev) => ({ ...prev, [field]: value }));

    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    setErrorMessage('');
  };

  const validateForm = () => {
    const nextErrors: LoginErrors = {};

    const usernameResult = validateUsername(formData.name.trim());
    if (!usernameResult.valid) {
      nextErrors.name = usernameResult.message ?? '请输入有效用户名';
    }

    const passwordResult = validatePassword(formData.password);
    if (!passwordResult.valid) {
      nextErrors.password = passwordResult.message ?? '请输入有效密码';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setErrorMessage('');

    try {
      if (loginType === 'user') {
        const response = await authorLogin(formData);
        const data: AuthorLoginResponse | undefined = response.data?.data;
        const token = data?.accessToken;

        if (!token) {
          throw new Error('登录响应缺少访问令牌');
        }

        login(token, {
          id: parseUserIdFromToken(token) || formData.name,
          username: formData.name,
          role: USER_ROLE.USER,
        });

        success('登录成功');
        navigate(from === ROUTES.LOGIN ? ROUTES.HOME : from, { replace: true });
        return;
      }

      const response = await adminLogin(formData);
      const data: AdminLoginResponse | undefined = response.data?.data;
      const token = data?.token;

      if (!token) {
        throw new Error('登录响应缺少访问令牌');
      }

      login(token, {
        id: String(data?.admin?.id ?? formData.name),
        username: data?.admin?.username ?? formData.name,
        role: USER_ROLE.ADMIN,
      });

      success('登录成功');
      navigate(ROUTES.ADMIN_DASHBOARD, { replace: true });
    } catch (error: unknown) {
      const message = getErrorMessage(error, '登录失败，请检查用户名和密码');
      setErrorMessage(message);
      showError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleLoginTypeChange = (type: LoginType) => {
    setLoginType(type);
    setErrors({});
    setErrorMessage('');
  };

  const copy = LOGIN_META[loginType];

  return (
    <div className={styles.page}>
      <div className={styles.shell}>
        <section className={styles.brand}>
          <Link to={ROUTES.HOME} className={styles.logo}>
            <span className={styles.logoMark}>B</span>
            <span className={styles.logoText}>博文</span>
          </Link>

          <p className={styles.kicker}>创作者空间</p>
          <h1 className={styles.headline}>在一个入口里继续你的内容创作与站点运营</h1>
          <p className={styles.description}>
            登录后可管理文章、互动与账户设置。用户与管理员使用同一套账号体系，切换顺畅且安全。
          </p>

          <ul className={styles.points}>
            <li>文章发布、收藏、关注状态实时同步</li>
            <li>统一账号体系，支持用户与管理员双入口</li>
            <li>沿用站点同一设计语言与交互节奏</li>
          </ul>
        </section>

        <section className={styles.panel}>
          <div className={styles.header}>
            <h2 className={styles.title}>{copy.title}</h2>
            <p className={styles.subtitle}>{copy.subtitle}</p>
          </div>

          <div className={styles.switch}>
            <button
              type="button"
              onClick={() => handleLoginTypeChange('user')}
              className={`${styles.switchItem} ${loginType === 'user' ? styles.switchItemActive : ''}`}
            >
              用户登录
            </button>
            <button
              type="button"
              onClick={() => handleLoginTypeChange('admin')}
              className={`${styles.switchItem} ${loginType === 'admin' ? styles.switchItemActive : ''}`}
            >
              管理员登录
            </button>
          </div>

          <form className={styles.form} onSubmit={handleSubmit}>
            <Input
              label="用户名"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange('name')}
              error={errors.name}
              autoComplete="username"
              placeholder={loginType === 'admin' ? '请输入管理员用户名' : '请输入用户名'}
              required
            />

            <Input
              label="密码"
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleInputChange('password')}
              error={errors.password}
              autoComplete="current-password"
              placeholder="请输入密码"
              required
            />

            {errorMessage && (
              <div className={styles.errorMessage}>
                <svg viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                    clipRule="evenodd"
                  />
                </svg>
                <p>{errorMessage}</p>
              </div>
            )}

            <Button
              type="submit"
              variant="primary"
              size="large"
              fullWidth
              loading={loading}
              className={styles.submit}
            >
              {copy.submitText}
            </Button>
          </form>

          {loginType === 'user' ? (
            <p className={styles.hint}>
              还没有账号？
              <button type="button" className={styles.linkButton} onClick={() => navigate(ROUTES.REGISTER)}>
                立即注册
              </button>
            </p>
          ) : (
            <p className={styles.hint}>
              访问普通用户入口？
              <button type="button" className={styles.linkButton} onClick={() => handleLoginTypeChange('user')}>
                切换到用户登录
              </button>
            </p>
          )}
        </section>
      </div>
    </div>
  );
};

export default Login;
