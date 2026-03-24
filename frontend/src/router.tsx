import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthGuard } from './guards/AuthGuard';
import { AdminGuard } from './guards/AdminGuard';
import { Loading } from './components/common/Loading';
import { ROUTES } from './utils/constants';

// Eagerly loaded (critical path)
import Login from './pages/Login';

// Lazy loaded pages
const Register = lazy(() => import('./pages/auth/Register'));
const Home = lazy(() => import('./pages/home/Home'));
const ArticleDetail = lazy(() => import('./pages/article/ArticleDetail'));
const ArticleEditor = lazy(() => import('./pages/article/ArticleEditor'));
const CategoryBrowse = lazy(() => import('./pages/article/CategoryBrowse'));

// User pages
const UserProfile = lazy(() => import('./pages/user/UserProfile'));
const UserArticles = lazy(() => import('./pages/user/UserArticles'));
const UserFavorites = lazy(() => import('./pages/user/UserFavorites'));
const UserFollows = lazy(() => import('./pages/user/UserFollows'));
const UserSettings = lazy(() => import('./pages/user/UserSettings'));

// User author page (public)
const UserAuthorPage = lazy(() => import('./pages/user/UserAuthorPage'));
const NotFound = lazy(() => import('./pages/NotFound/NotFound'));

// Admin pages
const AdminLogin = lazy(() => import('./pages/admin/AdminLogin'));
const AdminDashboard = lazy(() => import('./pages/admin/AdminDashboard'));
const AdminUsers = lazy(() => import('./pages/admin/AdminUsers'));
const AdminCategories = lazy(() => import('./pages/admin/AdminCategories'));
const AdminArticles = lazy(() => import('./pages/admin/AdminArticles'));

const fallback = <Loading fullscreen />;

export const AppRouter: React.FC = () => (
  <BrowserRouter>
    <Suspense fallback={fallback}>
      <Routes>
        {/* Public */}
        <Route path={ROUTES.HOME} element={<Home />} />
        <Route path={ROUTES.LOGIN} element={<Login />} />
        <Route path={ROUTES.REGISTER} element={<Register />} />
        <Route path={ROUTES.ARTICLE_DETAIL} element={<ArticleDetail />} />
        <Route path={ROUTES.CATEGORY} element={<CategoryBrowse />} />
        <Route path={ROUTES.AUTHOR_DETAIL} element={<UserAuthorPage />} />

        {/* Auth required */}
        <Route path={ROUTES.ARTICLE_NEW} element={
          <AuthGuard><ArticleEditor /></AuthGuard>
        } />
        <Route path={ROUTES.ARTICLE_EDIT} element={
          <AuthGuard><ArticleEditor /></AuthGuard>
        } />

        {/* User profile */}
        <Route path={ROUTES.PROFILE} element={<AuthGuard><UserProfile /></AuthGuard>} />
        <Route path={ROUTES.PROFILE_ARTICLES} element={<AuthGuard><UserArticles /></AuthGuard>} />
        <Route path={ROUTES.PROFILE_FAVORITES} element={<AuthGuard><UserFavorites /></AuthGuard>} />
        <Route path={ROUTES.PROFILE_FOLLOWS} element={<AuthGuard><UserFollows /></AuthGuard>} />
        <Route path={ROUTES.PROFILE_SETTINGS} element={<AuthGuard><UserSettings /></AuthGuard>} />

        {/* Admin */}
        <Route path={ROUTES.ADMIN_LOGIN} element={<AdminLogin />} />
        <Route path={ROUTES.ADMIN_DASHBOARD} element={
          <AdminGuard><AdminDashboard /></AdminGuard>
        } />
        <Route path={ROUTES.ADMIN_USERS} element={
          <AdminGuard><AdminUsers /></AdminGuard>
        } />
        <Route path={ROUTES.ADMIN_CATEGORIES} element={
          <AdminGuard><AdminCategories /></AdminGuard>
        } />
        <Route path={ROUTES.ADMIN_ARTICLES} element={
          <AdminGuard><AdminArticles /></AdminGuard>
        } />
        <Route path={ROUTES.ADMIN_ARTICLE_EDIT} element={
          <AdminGuard><ArticleEditor /></AdminGuard>
        } />

        {/* Fallback */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  </BrowserRouter>
);
