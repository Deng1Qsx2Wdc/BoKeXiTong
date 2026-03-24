import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardGetAllTotal, dashboardGetCategoryArticleTotal } from '../../api/dashboard';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Loading } from '../../components/common/Loading';
import { ROUTES } from '../../utils/constants';
import type { Cate_Ari_Total, Dashboard } from '../../types';
import styles from './AdminDashboard.module.css';

const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<Dashboard | null>(null);
  const [cateStats, setCateStats] = useState<Cate_Ari_Total[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.allSettled([
      dashboardGetAllTotal().then((response) => setStats(response.data.data ?? null)),
      dashboardGetCategoryArticleTotal(0).then((response) => setCateStats(response.data.data ?? [])),
    ]).finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <PageLayout><Loading /></PageLayout>;
  }

  return (
    <PageLayout>
      <Container>
        <div className={styles.header}>
          <h1 className={styles.title}>管理仪表盘</h1>
          <nav className={styles.adminnav}>
            <Link to={ROUTES.ADMIN_USERS} className={styles.adminlink}>用户管理</Link>
            <Link to={ROUTES.ADMIN_CATEGORIES} className={styles.adminlink}>分类管理</Link>
            <Link to={ROUTES.ADMIN_ARTICLES} className={styles.adminlink}>文章管理</Link>
          </nav>
        </div>

        {stats && (
          <div className={styles.stats}>
            <div className={styles.stat}>
              <span className={styles.num}>{stats.articleTotal}</span>
              <span className={styles.label}>文章总数</span>
            </div>
            <div className={styles.stat}>
              <span className={styles.num}>{stats.authorTotal}</span>
              <span className={styles.label}>用户总数</span>
            </div>
            <div className={styles.stat}>
              <span className={styles.num}>{stats.categoryTotal}</span>
              <span className={styles.label}>分类总数</span>
            </div>
            <div className={styles.stat}>
              <span className={styles.num}>{stats.latest_seven_days_article_total}</span>
              <span className={styles.label}>7 日新增文章</span>
            </div>
          </div>
        )}

        {cateStats.length > 0 && (
          <div className={styles.section}>
            <h2 className={styles['section-title']}>分类文章分布</h2>
            <div className={styles.cate_list}>
              {cateStats.map((item) => (
                <div key={item.id} className={styles.cate_item}>
                  <span className={styles.cate_name}>{item.name}</span>
                  <div className={styles.bar_wrap}>
                    <div
                      className={styles.bar}
                      style={{ width: `${Math.min(100, (item.count / (stats?.articleTotal || 1)) * 100)}%` }}
                    />
                  </div>
                  <span className={styles.cate_num}>{item.count}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </Container>
    </PageLayout>
  );
};

export default AdminDashboard;
