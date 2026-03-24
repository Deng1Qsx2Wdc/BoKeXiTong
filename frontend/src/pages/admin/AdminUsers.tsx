import React, { useCallback, useEffect, useState } from 'react';
import { adminDeleteUser, adminListUsers } from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { Loading } from '../../components/common/Loading';
import { Pagination } from '../../components/common/Pagination';
import { useToast } from '../../context/ToastContext';
import type { Author } from '../../types';
import { PAGINATION } from '../../utils/constants';
import styles from './AdminUsers.module.css';

const AdminUsers: React.FC = () => {
  const { success, error } = useToast();
  const [users, setUsers] = useState<Author[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState<number>(PAGINATION.DEFAULT_PAGE);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const pageSize = PAGINATION.ADMIN_PAGE_SIZE;

  const fetchUsers = useCallback(() => {
    setLoading(true);
    adminListUsers(page, pageSize, search)
      .then((response) => {
        setUsers(response.data.data?.records ?? []);
        setTotal(response.data.data?.total ?? 0);
      })
      .catch((err: unknown) => error(getErrorMessage(err, '加载用户失败')))
      .finally(() => setLoading(false));
  }, [error, page, search]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定删除这个用户吗？此操作不可恢复。')) return;

    try {
      await adminDeleteUser(id);
      success('用户已删除');
      fetchUsers();
    } catch (err: unknown) {
      error(getErrorMessage(err, '删除失败'));
    }
  };

  return (
    <PageLayout>
      <Container>
        <h1 className={styles.title}>用户管理</h1>

        <div className={styles.toolbar}>
          <Input
            placeholder="搜索用户名"
            value={search}
            onChange={(event) => {
              setSearch((event.target as HTMLInputElement).value);
              setPage(PAGINATION.DEFAULT_PAGE);
            }}
          />
        </div>

        {loading ? <Loading /> : (
          <>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>
                      <Button size="small" variant="ghost" onClick={() => handleDelete(user.id)}>删除</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination current={page} total={total} pageSize={pageSize} onChange={setPage} />
          </>
        )}
      </Container>
    </PageLayout>
  );
};

export default AdminUsers;
