import React, { useState, useEffect, useRef } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Button } from '../../common/Button';
import { SearchBar } from '../../common/SearchBar/SearchBar';
import { NotificationBell } from '../../common/NotificationBell/NotificationBell';
import { ROUTES } from '../../../utils/constants';
import styles from './Header.module.css';

export const Header: React.FC = () => {
  const { isAuthenticated, isAdmin, user: userInfo, logout } = useAuth();
  const navigate = useNavigate();
  const [scrolled, setScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
    setDropdownOpen(false);
  };

  const avatarLetter = userInfo?.username?.[0]?.toUpperCase() ?? 'U';

  return (
    <header className={`${styles.header} ${scrolled ? styles['header-scrolled'] : ''}`}>
      <div className={`${styles['header-inner']} container-lg`} style={{ margin: '0 auto' }}>
        <Link to={ROUTES.HOME} className={styles.logo}>
          <div className={styles['logo-mark']}>B</div>
          <span className={styles['logo-text']}>博文</span>
        </Link>

        <nav className={styles.nav}>
          <NavLink
              to={ROUTES.HOME}
            end
            className={({ isActive }) =>
              `${styles['nav-link']} ${isActive ? styles['nav-link-active'] : ''}`
            }
          >
            首页
          </NavLink>
          {isAuthenticated && !isAdmin && (
            <NavLink
              to={ROUTES.ARTICLE_NEW}
              className={({ isActive }) =>
                `${styles['nav-link']} ${isActive ? styles['nav-link-active'] : ''}`
              }
            >
              写文章
            </NavLink>
          )}
          {isAdmin && (
            <NavLink
              to={ROUTES.ADMIN_DASHBOARD}
              className={({ isActive }) =>
                `${styles['nav-link']} ${isActive ? styles['nav-link-active'] : ''}`
              }
            >
              管理后台
            </NavLink>
          )}
        </nav>

        <div className={styles['header-actions']}>
          <SearchBar />
          <NotificationBell />
          {isAuthenticated ? (
            <div className={styles['user-menu']} ref={dropdownRef}>
              <button
                className={styles['user-button']}
                onClick={() => setDropdownOpen((v) => !v)}
              >
                <div className={styles['user-avatar']}>{avatarLetter}</div>
                <span className={styles['user-name']}>{userInfo?.username}</span>
              </button>
              {dropdownOpen && (
                <div className={styles.dropdown}>
                  {!isAdmin && (
                    <>
                      <Link
                        to={ROUTES.PROFILE}
                        className={styles['dropdown-item']}
                        onClick={() => setDropdownOpen(false)}
                      >
                        个人中心
                      </Link>
                      <Link
                        to={ROUTES.PROFILE_ARTICLES}
                        className={styles['dropdown-item']}
                        onClick={() => setDropdownOpen(false)}
                      >
                        我的文章
                      </Link>
                      <div className={styles['dropdown-divider']} />
                    </>
                  )}
                  <button
                    className={`${styles['dropdown-item']} ${styles['dropdown-item-danger']}`}
                    onClick={handleLogout}
                  >
                    退出登录
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Button variant="ghost" size="small" onClick={() => navigate(ROUTES.LOGIN)}>
                登录
              </Button>
              <Button variant="primary" size="small" onClick={() => navigate(ROUTES.REGISTER)}>
                注册
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
