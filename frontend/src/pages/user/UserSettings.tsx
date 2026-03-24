import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { authorUpdate } from '../../api/author';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { useToast } from '../../context/ToastContext';
import styles from './UserSettings.module.css';

const UserSettings: React.FC = () => {
  const { user, logout } = useAuth();
  const { success, error } = useToast();
  const [username, setUsername] = useState(user?.username ?? '');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.id) return;
    if (password && password !== confirm) { error('两次密码不一致'); return; }
    if (password && (password.length < 6 || password.length > 20)) { error('密码长度6-20位'); return; }
    setSaving(true);
    try {
      await authorUpdate({ id: user.id, username, ...(password ? { password } : {}) });
      success('保存成功，请重新登录');
      setTimeout(() => logout(), 1500);
    } catch { error('保存失败'); }
    finally { setSaving(false); }
  };

  return (
    <PageLayout>
      <Container>
        <div className={styles.wrap}>
          <h1 className={styles.title}>账号设置</h1>
          <form onSubmit={handleSave} className={styles.form}>
            <Input
              label="用户名"
              value={username}
              onChange={(e) => setUsername((e.target as HTMLInputElement).value)}
              required
            />
            <Input
              label="新密码（留空不修改）"
              type="password"
              value={password}
              onChange={(e) => setPassword((e.target as HTMLInputElement).value)}
            />
            <Input
              label="确认新密码"
              type="password"
              value={confirm}
              onChange={(e) => setConfirm((e.target as HTMLInputElement).value)}
            />
            <div className={styles.footer}>
              <Button type="submit" variant="primary" loading={saving}>保存修改</Button>
              <Button type="button" variant="outline" onClick={logout}>退出登录</Button>
            </div>
          </form>
        </div>
      </Container>
    </PageLayout>
  );
};

export default UserSettings;
