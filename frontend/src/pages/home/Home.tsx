import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { articleQuery } from '../../api/article';
import { categoryQuery } from '../../api/category';
import { authorQueryOneById } from '../../api/author';
import type { Article, Category, Id } from '../../types';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import { Container } from '../../components/layout/Container';
import { ArticleCard } from '../../components/article/ArticleCard';
import { Pagination } from '../../components/common/Pagination';
import { Loading } from '../../components/common/Loading';
import { ARTICLE_STATUS, PAGINATION } from '../../utils/constants';
import styles from './Home.module.css';

const PAGE_SIZE_HOME = PAGINATION.DEFAULT_PAGE_SIZE;

const parsePositiveInt = (value: string | null) => {
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
};

const Home: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = parsePositiveInt(searchParams.get('page')) ?? PAGINATION.DEFAULT_PAGE;
  const selectedCategory = searchParams.get('category')?.trim() || undefined;
  const keyword = searchParams.get('keyword')?.trim() || undefined;

  const [articles, setArticles] = useState<Article[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [authorMap, setAuthorMap] = useState<Record<Id, string>>({});
  const [categoryMap, setCategoryMap] = useState<Record<Id, Category>>({});
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    categoryQuery({ pageNum: PAGINATION.DEFAULT_PAGE, pageSize: PAGINATION.LARGE_PAGE_SIZE })
      .then((res) => {
        const cats = res.data.data?.records ?? [];
        setCategories(cats);
        const map: Record<Id, Category> = {};
        cats.forEach((category) => {
          map[category.id] = category;
        });
        setCategoryMap(map);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    articleQuery({
      pageNum: currentPage,
      pageSize: PAGE_SIZE_HOME,
      status: ARTICLE_STATUS.PUBLISHED,
      categoryId: selectedCategory,
      keyword,
    })
      .then(async (res) => {
        const page = res.data.data;
        const list = page?.records ?? [];
        setArticles(list);
        setTotal(page?.total ?? 0);

        const authorIds = [...new Set(list.map((article) => article.authorId))];
        const missingAuthorIds = authorIds.filter((authorId) => !authorMap[authorId]);

        if (missingAuthorIds.length === 0) {
          return;
        }

        const authorEntries = await Promise.all(
          missingAuthorIds.map(async (authorId) => {
            try {
              const authorRes = await authorQueryOneById(authorId);
              return [authorId, authorRes.data.data?.username ?? `用户${authorId}`] as const;
            } catch {
              return [authorId, `用户${authorId}`] as const;
            }
          }),
        );

        setAuthorMap((prev) => ({
          ...prev,
          ...Object.fromEntries(authorEntries),
        }));
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [currentPage, selectedCategory, keyword]);

  const handlePageChange = (page: number) => {
    const nextParams = new URLSearchParams(searchParams);
    nextParams.set('page', String(page));
    setSearchParams(nextParams);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleCategoryClick = (catId?: Id) => {
    const nextParams = new URLSearchParams(searchParams);

    if (!catId) {
      nextParams.delete('category');
    } else {
      nextParams.set('category', catId);
    }

    nextParams.set('page', '1');
    setSearchParams(nextParams);
  };

  return (
    <PageLayout>
      <Container>
        <div className={styles.layout}>
          <main className={styles.main}>
            <div className={styles['section-header']}>
              <div className={styles['title-wrap']}>
                <h1 className={styles['section-title']}>
                  {keyword
                    ? `搜索：${keyword}`
                    : selectedCategory && categoryMap[selectedCategory]
                      ? categoryMap[selectedCategory].name
                      : '最新文章'}
                </h1>
                {keyword && (
                  <p className={styles['search-hint']}>
                    已按标题、正文、文章 ID、作者 ID 进行综合检索
                  </p>
                )}
              </div>
              {total > 0 && (
                <span className={styles['article-count']}>{total} 篇</span>
              )}
            </div>

            {loading ? (
              <Loading />
            ) : articles.length === 0 ? (
              <div className={styles.empty}>
                <p className={styles['empty-text']}>暂无文章</p>
              </div>
            ) : (
              <div className={styles['article-list']}>
                {articles.map((article, i) => (
                  <ArticleCard
                    key={article.id}
                    article={article}
                    category={categoryMap[article.categoryId]}
                    authorName={authorMap[article.authorId]}
                    index={i}
                  />
                ))}
              </div>
            )}

            <Pagination
              current={currentPage}
              total={total}
              pageSize={PAGE_SIZE_HOME}
              onChange={handlePageChange}
            />
          </main>

          <aside className={styles.sidebar}>
            <div className={styles['sidebar-section']}>
              <h3 className={styles['sidebar-title']}>分类</h3>
              <ul className={styles['category-list']}>
                <li>
                  <button
                    className={`${styles['category-item']} ${
                      !selectedCategory ? styles['category-item-active'] : ''
                    }`}
                    onClick={() => handleCategoryClick()}
                  >
                    全部
                  </button>
                </li>
                {categories.map((cat) => (
                  <li key={cat.id}>
                    <button
                      className={`${styles['category-item']} ${
                        selectedCategory === cat.id ? styles['category-item-active'] : ''
                      }`}
                      onClick={() => handleCategoryClick(cat.id)}
                    >
                      {cat.name}
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          </aside>
        </div>
      </Container>
    </PageLayout>
  );
};

export default Home;
