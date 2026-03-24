import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminLogin } from '../../api/admin';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { ROUTES, USER_ROLE } from '../../utils/constants';
import styles from './AdminLogin.module.css';

const AdminLogin: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { success, error } = useToast();
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !password) { error('请填写用户名和密码'); return; }
    setLoading(true);
    try {
      const res = await adminLogin({ name, password });
      const data = res.data.data;
      if (data) {
        login(data.token, { id: data.admin.id, username: data.admin.username, role: USER_ROLE.ADMIN });
        success('登录成功');
        navigate(ROUTES.ADMIN_DASHBOARD);
      }
    } catch { error('用户名或密码错误'); }
    finally { setLoading(false); }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>管理员登录</h1>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>用户名</label>
            <input className={styles.input} value={name} onChange={(e) => setName(e.target.value)} autoFocus />
          </div>
          <div className={styles.field}>
            <label className={styles.label}>密码</label>
            <input className={styles.input} type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <button type="submit" className={styles.btn} disabled={loading}>
            {loading ? '登录中…' : '登录'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AdminLogin;
