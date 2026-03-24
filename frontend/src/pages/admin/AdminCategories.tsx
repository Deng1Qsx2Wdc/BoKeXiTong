import React, { useEffect, useState } from 'react';
import {
  adminCategoryQuery,
  adminCategoryInsert,
  adminCategoryUpdate,
  adminCategoryDelete,
} from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { Modal } from '../../components/common/Modal';
import { Loading } from '../../components/common/Loading';
import { useToast } from '../../context/ToastContext';
import type { Category } from '../../types';
import { PAGINATION } from '../../utils/constants';
import styles from './AdminCategories.module.css';

const AdminCategories: React.FC = () => {
  const { success, error } = useToast();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Category | null>(null);
  const [name, setName] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchCategories = () => {
    setLoading(true);
    adminCategoryQuery({ pageNum: PAGINATION.DEFAULT_PAGE, pageSize: PAGINATION.LARGE_PAGE_SIZE })
      .then((response) => setCategories(response.data.data?.records ?? []))
      .catch((err: unknown) => error(getErrorMessage(err, '加载分类失败')))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const openNew = () => {
    setEditing(null);
    setName('');
    setModalOpen(true);
  };

  const openEdit = (category: Category) => {
    setEditing(category);
    setName(category.name);
    setModalOpen(true);
  };

  const handleSave = async () => {
    if (!name.trim()) {
      error('分类名称不能为空');
      return;
    }

    setSaving(true);
    try {
      if (editing) {
        await adminCategoryUpdate({ id: editing.id, name: name.trim() });
        success('分类已更新');
      } else {
        await adminCategoryInsert({ name: name.trim() });
        success('分类已添加');
      }

      setModalOpen(false);
      fetchCategories();
    } catch (err: unknown) {
      error(getErrorMessage(err, '操作失败'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定删除这个分类吗？')) return;

    try {
      await adminCategoryDelete(id);
      success('分类已删除');
      fetchCategories();
    } catch (err: unknown) {
      error(getErrorMessage(err, '删除失败'));
    }
  };

  return (
    <PageLayout>
      <Container>
        <div className={styles.header}>
          <h1 className={styles.title}>分类管理</h1>
          <Button variant="primary" onClick={openNew}>添加分类</Button>
        </div>

        {loading ? <Loading /> : (
          <div className={styles.list}>
            {categories.map((category) => (
              <div key={category.id} className={styles.item}>
                <span className={styles['item-name']}>{category.name}</span>
                <div className={styles['item-actions']}>
                  <Button size="small" variant="outline" onClick={() => openEdit(category)}>编辑</Button>
                  <Button size="small" variant="ghost" onClick={() => handleDelete(category.id)}>删除</Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Container>

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? '编辑分类' : '添加分类'}
        footer={(
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>取消</Button>
            <Button variant="primary" loading={saving} onClick={handleSave}>保存</Button>
          </>
        )}
      >
        <Input
          label="分类名称"
          value={name}
          onChange={(event) => setName((event.target as HTMLInputElement).value)}
          required
          autoFocus
        />
      </Modal>
    </PageLayout>
  );
};

export default AdminCategories;
