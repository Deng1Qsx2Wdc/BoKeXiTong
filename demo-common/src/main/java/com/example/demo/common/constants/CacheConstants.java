package com.example.demo.common.constants;

/**
 * Redis cache names and shared cache keys.
 */
public final class CacheConstants {

    private CacheConstants() {
    }

    public static final String ARTICLE_QUERY_CACHE_VERSION = "articleQuery:version";

    public static final String CATEGORY = "category";
    public static final String CATEGORY_ARTICLE_TOTAL = "category_on_article_total";

    public static final String ARTICLE_DETAIL = "Article:detail";

    public static final String AUTHOR = "Author";
    public static final String AUTHOR_SEARCH_LIST = "Author:Search:List";
    public static final String AUTHOR_ALL_MESSAGE = "Author:All:Message";

    public static final String COMMENT = "comment";

    public static final String AUTHOR_THUMBSUP_LIST = "author:thumbsup:list";
    public static final String THUMBSUP_ARTICLE_AUTHOR_LIST = "thumbsup:article:author:list";
    public static final String AUTHOR_FAVORITES_LIST = "author:favorites:article:list";
    public static final String AUTHOR_FOLLOW = "author:follow";
    public static final String DASHBOARD_COUNT = "Dashboard:Count";
}
