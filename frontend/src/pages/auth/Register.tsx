import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authorRegister } from '../../api/author';
import { getErrorMessage } from '../../api/client';
import { useToast } from '../../context/ToastContext';
import { Input } from '../../components/common/Input';
import { Button } from '../../components/common/Button';
import { ROUTES } from '../../utils/constants';
import { validateUsername, validatePassword } from '../../utils/validation';
import styles from './Register.module.css';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const { success, error } = useToast();
  const [form, setForm] = useState({ name: '', password: '', confirm: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const e: Record<string, string> = {};
    const uRes = validateUsername(form.name);
    const pRes = validatePassword(form.password);
    if (!uRes.valid) e.name = uRes.message ?? '用户名不合法';
    if (!pRes.valid) e.password = pRes.message ?? '密码不合法';
    if (!form.confirm) e.confirm = '请确认密码';
    else if (form.confirm !== form.password) e.confirm = '两次输入的密码不一致';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      await authorRegister({ name: form.name, password: form.password });
      success('注册成功，请登录');
      navigate(ROUTES.LOGIN);
    } catch (err: unknown) {
      error(getErrorMessage(err, '注册失败，请重试'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.header}>
          <Link to={ROUTES.HOME} className={styles['logo-link']}>
            <span className={styles['logo-mark']}>B</span>
            <span className={styles['logo-text']}>博文</span>
          </Link>
          <h1 className={styles.title}>创建账户</h1>
          <p className={styles.subtitle}>加入博文，开始你的写作之旅</p>
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          <Input
            label="用户名"
            name="name"
            value={form.name}
            onChange={handleChange('name')}
            error={errors.name}
            placeholder="3-20个字符"
            required
            autoComplete="username"
          />
          <Input
            label="密码"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange('password')}
            error={errors.password}
            placeholder="6-20个字符"
            required
            autoComplete="new-password"
          />
          <Input
            label="确认密码"
            name="confirm"
            type="password"
            value={form.confirm}
            onChange={handleChange('confirm')}
            error={errors.confirm}
            placeholder="再次输入密码"
            required
            autoComplete="new-password"
          />

          <Button
            type="submit"
            variant="primary"
            size="large"
            loading={loading}
            className={styles['submit-btn']}
            style={{ width: '100%' }}
          >
            注册
          </Button>
        </form>

        <p className={styles['login-hint']}>
          已有账户？
          <Link to={ROUTES.LOGIN} className={styles['login-link']}>立即登录</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
