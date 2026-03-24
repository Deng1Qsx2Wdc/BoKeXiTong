import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { followsGetFollowList, followsGetFollowerList, followsUnfollow } from '../../api/follows';
import { authorQueryOneById } from '../../api/author';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { Button } from '../../components/common/Button';
import { Loading } from '../../components/common/Loading';
import { useToast } from '../../context/ToastContext';
import type { Follows, Id } from '../../types';
import { ROUTES } from '../../utils/constants';
import styles from './UserFollows.module.css';

const UserFollows: React.FC = () => {
  const { user } = useAuth();
  const { success, error } = useToast();
  const [tab, setTab] = useState<'following' | 'followers'>('following');
  const [following, setFollowing] = useState<Follows[]>([]);
  const [followers, setFollowers] = useState<Follows[]>([]);
  const [nameMap, setNameMap] = useState<Record<Id, string>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user?.id) return;
    setLoading(true);
    Promise.allSettled([
      followsGetFollowList(user.id).then((r) => setFollowing(r.data.data?.records ?? [])),
      followsGetFollowerList(user.id).then((r) => setFollowers(r.data.data ?? [])),
    ]).finally(() => setLoading(false));
  }, [user?.id]);

  useEffect(() => {
    const ids = [...new Set([
      ...following.map((item) => item.targetId),
      ...followers.map((item) => item.authorId),
    ])];

    const missingIds = ids.filter((id) => !nameMap[id]);
    if (missingIds.length === 0) return;

    Promise.allSettled(
      missingIds.map(async (id) => {
        const res = await authorQueryOneById(id);
        return [id, res.data.data?.username ?? `用户${id}`] as const;
      }),
    ).then((results) => {
      const entries = results
        .filter((result): result is PromiseFulfilledResult<readonly [Id, string]> => result.status === 'fulfilled')
        .map((result) => result.value);

      if (entries.length === 0) return;
      setNameMap((prev) => ({
        ...prev,
        ...Object.fromEntries(entries),
      }));
    });
  }, [followers, following, nameMap]);

  const handleUnfollow = async (targetId: Id) => {
    if (!user?.id) return;
    try {
      await followsUnfollow({ authorId: user.id, targetId });
      setFollowing((prev) => prev.filter((follow) => follow.targetId !== targetId));
      success('已取消关注');
    } catch {
      error('操作失败');
    }
  };

  if (loading) return <PageLayout><Loading /></PageLayout>;

  const list = tab === 'following' ? following : followers;

  return (
    <PageLayout>
      <Container>
        <h1 className={styles.title}>关注</h1>
        <div className={styles.tabs}>
          <button className={`${styles.tab} ${tab === 'following' ? styles.active : ''}`} onClick={() => setTab('following')}>我的关注 ({following.length})</button>
          <button className={`${styles.tab} ${tab === 'followers' ? styles.active : ''}`} onClick={() => setTab('followers')}>我的粉丝 ({followers.length})</button>
        </div>
        {list.length === 0 ? (
          <p className={styles.empty}>{tab === 'following' ? '还没有关注任何人' : '还没有粉丝'}</p>
        ) : (
          <div className={styles.list}>
            {list.map((item) => {
              const id = tab === 'following' ? item.targetId : item.authorId;
              const username = nameMap[id] ?? `用户${id}`;

              return (
                <div key={item.id} className={styles.item}>
                  <div className={styles.avatar}>{username[0]?.toUpperCase() ?? 'U'}</div>
                  <Link to={ROUTES.AUTHOR_DETAIL.replace(':id', String(id))} className={styles.name}>{username}</Link>
                  {tab === 'following' && (
                    <Button size="small" variant="outline" onClick={() => handleUnfollow(item.targetId)}>取消关注</Button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </Container>
    </PageLayout>
  );
};

export default UserFollows;
