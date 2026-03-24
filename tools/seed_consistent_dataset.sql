SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_num;
CREATE TEMPORARY TABLE tmp_num (
    n INT PRIMARY KEY
) ENGINE=Memory
AS
SELECT ones.n
       + tens.n * 10
       + hundreds.n * 100
       + thousands.n * 1000
       + tenthousands.n * 10000 AS n
FROM (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) hundreds
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) thousands
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tenthousands;

SET @target_authors = 500;
SET @target_articles = 10000;
SET @author_count_before = (SELECT COUNT(*) FROM author);
SET @article_count_before = (SELECT COUNT(*) FROM article);
SET @need_authors = GREATEST(@target_authors - @author_count_before, 0);
SET @need_articles = GREATEST(@target_articles - @article_count_before, 0);

SET @author_id_base = GREATEST(IFNULL((SELECT MAX(id) FROM author), 0), 2026032200000000000);

INSERT INTO author (id, username, password)
SELECT
    @author_id_base + t.n + 1 AS id,
    CONCAT(
        ELT(MOD(t.n, 12) + 1,
            'Liu', 'Wang', 'Zhang', 'Li', 'Chen', 'Yang',
            'Zhao', 'Huang', 'Zhou', 'Xu', 'Sun', 'Wu'
        ),
        ELT(MOD(FLOOR(t.n / 12), 10) + 1,
            'Ming', 'Lan', 'Jie', 'Rui', 'Tao',
            'Xin', 'Yun', 'Han', 'Qing', 'Yao'
        ),
        '_',
        LPAD(@author_count_before + t.n + 1, 4, '0')
    ) AS username,
    '$2a$10$zu/y4bExPPEZCaSfwK8wcemSFtiBL72DwFJZIFd7qWVhX9Vvou5lS' AS password
FROM tmp_num t
WHERE t.n < @need_authors;

DROP TEMPORARY TABLE IF EXISTS tmp_author_pool;
CREATE TEMPORARY TABLE tmp_author_pool
AS
SELECT
    ROW_NUMBER() OVER (ORDER BY id) AS seq,
    id
FROM (
    SELECT id
    FROM author
    ORDER BY id
    LIMIT 500
) a;

ALTER TABLE tmp_author_pool
ADD PRIMARY KEY (seq),
ADD KEY idx_author_id (id);

SET @author_pool_count = (SELECT COUNT(*) FROM tmp_author_pool);

DROP TEMPORARY TABLE IF EXISTS tmp_category_pool;
CREATE TEMPORARY TABLE tmp_category_pool
AS
SELECT
    ROW_NUMBER() OVER (ORDER BY id) AS seq,
    id
FROM category
ORDER BY id;

ALTER TABLE tmp_category_pool ADD PRIMARY KEY (seq);
SET @category_count = (SELECT COUNT(*) FROM tmp_category_pool);

SET @article_id_base = GREATEST(IFNULL((SELECT MAX(id) FROM article), 0), 2026032201000000000);

INSERT INTO article (
    id,
    category_id,
    title,
    content,
    author_id,
    create_time,
    update_time,
    status,
    thumbs_up,
    favorites
)
SELECT
    @article_id_base + t.n + 1 AS id,
    cp.id AS category_id,
    CONCAT(
        ELT(MOD(t.n, 14) + 1,
            '内容增长',
            '可观测性建设',
            '工程效率',
            '系统稳定性',
            '前端体验优化',
            '数据质量治理',
            '搜索相关性',
            '社区运营',
            'AI 写作流程',
            '安全基线',
            '发布管理',
            '缓存策略',
            '指标治理',
            '知识库建设'
        ),
        '｜',
        ELT(MOD(FLOOR(t.n / 14), 8) + 1,
            '实践笔记',
            '实施复盘',
            '团队手册',
            '风险评估',
            '运维清单',
            '迁移总结',
            '优化日志',
            '架构备忘录'
        ),
        '（第',
        LPAD(@article_count_before + t.n + 1, 5, '0'),
        '篇）'
    ) AS title,
    CONCAT(
        '概述：',
        ELT(MOD(t.n, 14) + 1,
            '内容增长',
            '可观测性建设',
            '工程效率',
            '系统稳定性',
            '前端体验优化',
            '数据质量治理',
            '搜索相关性',
            '社区运营',
            'AI 写作流程',
            '安全基线',
            '发布管理',
            '缓存策略',
            '指标治理',
            '知识库建设'
        ),
        '已经成为产品与研发团队的长期议题。本文记录从现状诊断到稳定上线的完整过程。',
        '\n\n',
        '背景：随着业务流量与功能复杂度同步提升，团队既要保证系统可靠性，也要提升跨角色协作效率。',
        '\n',
        '方法：先定义可度量目标，再将任务拆成小里程碑，按周复盘并设置明确回滚条件。',
        '\n\n',
        '执行：每次改动都包含责任人、指标看板和异常跟进流程。面对数据波动与用户反馈不一致时，优先做可复现验证，再决定是否扩大变更范围。',
        '\n\n',
        '结论：稳定的迭代节奏和透明沟通，通常比一次性的大改动更能持续提升质量与效率。'
    ) AS content,
    ap.id AS author_id,
    TIMESTAMPADD(MINUTE, -MOD(t.n * 37, 60 * 24 * 330), NOW()) AS create_time,
    TIMESTAMPADD(
        MINUTE,
        MOD(t.n * 17, 60 * 24 * 10),
        TIMESTAMPADD(MINUTE, -MOD(t.n * 37, 60 * 24 * 330), NOW())
    ) AS update_time,
    CASE
        WHEN MOD(t.n, 20) = 0 THEN '2'
        WHEN MOD(t.n, 9) = 0 THEN '0'
        ELSE '1'
    END AS status,
    0 AS thumbs_up,
    0 AS favorites
FROM tmp_num t
JOIN tmp_author_pool ap ON ap.seq = MOD(t.n * 17 + 29, @author_pool_count) + 1
JOIN tmp_category_pool cp ON cp.seq = MOD(t.n, @category_count) + 1
WHERE t.n < @need_articles;

UPDATE article
SET status = '1'
WHERE status IS NULL OR TRIM(status) = '';

UPDATE article
SET create_time = COALESCE(create_time, NOW()),
    update_time = COALESCE(update_time, create_time);

DROP TEMPORARY TABLE IF EXISTS tmp_article_pool;
CREATE TEMPORARY TABLE tmp_article_pool
AS
SELECT
    ROW_NUMBER() OVER (ORDER BY id) AS seq,
    id,
    author_id,
    create_time
FROM (
    SELECT id, author_id, create_time
    FROM article
    ORDER BY id
    LIMIT 10000
) a;

ALTER TABLE tmp_article_pool
ADD PRIMARY KEY (seq),
ADD KEY idx_article_id (id),
ADD KEY idx_article_author (author_id);

UPDATE article a
JOIN tmp_article_pool ar ON ar.id = a.id
SET
    a.title = CONCAT(
        ELT(MOD(ar.seq - 1, 14) + 1,
            '内容增长',
            '可观测性建设',
            '工程效率',
            '系统稳定性',
            '前端体验优化',
            '数据质量治理',
            '搜索相关性',
            '社区运营',
            'AI 写作流程',
            '安全基线',
            '发布管理',
            '缓存策略',
            '指标治理',
            '知识库建设'
        ),
        '｜',
        ELT(MOD(FLOOR((ar.seq - 1) / 14), 8) + 1,
            '实践笔记',
            '实施复盘',
            '团队手册',
            '风险评估',
            '运维清单',
            '迁移总结',
            '优化日志',
            '架构备忘录'
        ),
        '（第',
        LPAD(ar.seq, 5, '0'),
        '篇）'
    ),
    a.content = CONCAT(
        '概述：',
        ELT(MOD(ar.seq - 1, 14) + 1,
            '内容增长',
            '可观测性建设',
            '工程效率',
            '系统稳定性',
            '前端体验优化',
            '数据质量治理',
            '搜索相关性',
            '社区运营',
            'AI 写作流程',
            '安全基线',
            '发布管理',
            '缓存策略',
            '指标治理',
            '知识库建设'
        ),
        '已经成为产品与研发团队的长期议题。本文记录从现状诊断到稳定上线的完整过程。',
        '\n\n',
        '背景：随着业务流量与功能复杂度同步提升，团队既要保证系统可靠性，也要提升跨角色协作效率。',
        '\n',
        '方法：先定义可度量目标，再将任务拆成小里程碑，按周复盘并设置明确回滚条件。',
        '\n\n',
        '执行：每次改动都包含责任人、指标看板和异常跟进流程。面对数据波动与用户反馈不一致时，优先做可复现验证，再决定是否扩大变更范围。',
        '\n\n',
        '结论：稳定的迭代节奏和透明沟通，通常比一次性的大改动更能持续提升质量与效率。'
    );

DROP TEMPORARY TABLE IF EXISTS tmp_small;
CREATE TEMPORARY TABLE tmp_small
AS
SELECT n
FROM tmp_num
WHERE n < 64;

ALTER TABLE tmp_small ADD PRIMARY KEY (n);

DELETE FROM thumbs_up;
DELETE FROM favorites;
DELETE FROM follows;
DELETE FROM comment;

SET @thumbs_id = 0;
INSERT INTO thumbs_up (id, author_id, article_id, thumbs_up_time, status)
SELECT
    (@thumbs_id := @thumbs_id + 1) AS id,
    liker.id AS author_id,
    ar.id AS article_id,
    TIMESTAMPADD(
        MINUTE,
        MOD(ar.seq * 31 + s.n * 13, 60 * 24 * 365),
        '2025-01-01 00:00:00'
    ) AS thumbs_up_time,
    1 AS status
FROM tmp_article_pool ar
JOIN tmp_small s ON s.n < (8 + MOD(ar.seq, 28))
JOIN tmp_author_pool liker ON liker.seq = MOD(ar.seq * 37 + s.n * 13, @author_pool_count) + 1
ORDER BY ar.seq, s.n;

SET @fav_id = 2026032209000000000;
INSERT INTO favorites (id, author_id, article_id, favorites_time, status)
SELECT
    (@fav_id := @fav_id + 1) AS id,
    fav_user.id AS author_id,
    ar.id AS article_id,
    TIMESTAMPADD(
        MINUTE,
        MOD(ar.seq * 19 + s.n * 23, 60 * 24 * 365),
        '2025-01-01 00:00:00'
    ) AS favorites_time,
    1 AS status
FROM tmp_article_pool ar
JOIN tmp_small s ON s.n < (3 + MOD(ar.seq, 12))
JOIN tmp_author_pool fav_user ON fav_user.seq = MOD(ar.seq * 71 + s.n * 17 + 7, @author_pool_count) + 1
ORDER BY ar.seq, s.n;

DROP TEMPORARY TABLE IF EXISTS tmp_author_pool_target;
CREATE TEMPORARY TABLE tmp_author_pool_target
AS
SELECT seq, id
FROM tmp_author_pool;

ALTER TABLE tmp_author_pool_target
ADD PRIMARY KEY (seq),
ADD KEY idx_author_id (id);

SET @follow_id = 2026032210000000000;
INSERT INTO follows (id, author_id, target_id, follow_time, status)
SELECT
    (@follow_id := @follow_id + 1) AS id,
    follower.id AS author_id,
    target.id AS target_id,
    TIMESTAMPADD(
        MINUTE,
        MOD(follower.seq * 53 + s.n * 29, 60 * 24 * 365),
        '2025-01-01 00:00:00'
    ) AS follow_time,
    1 AS status
FROM tmp_author_pool follower
JOIN tmp_small s ON s.n BETWEEN 1 AND 6
JOIN tmp_author_pool_target target
  ON target.seq = MOD((follower.seq - 1) + s.n * 13, @author_pool_count) + 1
ORDER BY follower.seq, s.n;

SET @comment_id = 2026032211000000000;
INSERT INTO comment (id, article_id, author_id, parent_id, content, create_time)
SELECT
    (@comment_id := @comment_id + 1) AS id,
    ar.id AS article_id,
    comment_user.id AS author_id,
    NULL AS parent_id,
    CONCAT(
        ELT(MOD(ar.seq + s.n, 10) + 1,
            '这篇写得很清楚，取舍讲得很到位。',
            '清单化的部分很实用，团队可以直接照着落地。',
            '和我们每周迭代的经验很像，读起来很有共鸣。',
            '对速度与稳定性的平衡思路很有参考价值。',
            '结构清晰，尤其是风险控制部分很扎实。',
            '先看指标再决策这个方法非常认同。',
            '执行方案现实可行，小团队也能照着做。',
            '关于回滚路径的提醒很关键，常被忽略。',
            '复盘节奏写得很好，值得长期坚持。',
            '对日常项目管理很有帮助，收藏了。'
        ),
        '（引用',
        ar.seq,
        '-',
        s.n,
        '）'
    ) AS content,
    TIMESTAMPADD(
        MINUTE,
        MOD(ar.seq * 3 + s.n * 23, 60 * 24 * 20),
        ar.create_time
    ) AS create_time
FROM tmp_article_pool ar
JOIN tmp_small s ON s.n < (2 + MOD(ar.seq, 5))
JOIN tmp_author_pool comment_user ON comment_user.seq = MOD(ar.seq * 29 + s.n * 7 + 11, @author_pool_count) + 1
ORDER BY ar.seq, s.n;

UPDATE article a
LEFT JOIN (
    SELECT article_id, COUNT(*) AS cnt
    FROM thumbs_up
    WHERE status = 1
    GROUP BY article_id
) t ON t.article_id = a.id
SET a.thumbs_up = IFNULL(t.cnt, 0);

UPDATE article a
LEFT JOIN (
    SELECT article_id, COUNT(*) AS cnt
    FROM favorites
    WHERE status = 1
    GROUP BY article_id
) f ON f.article_id = a.id
SET a.favorites = IFNULL(f.cnt, 0);

COMMIT;

SELECT 'author' AS table_name, COUNT(*) AS row_count FROM author
UNION ALL
SELECT 'article' AS table_name, COUNT(*) AS row_count FROM article
UNION ALL
SELECT 'thumbs_up' AS table_name, COUNT(*) AS row_count FROM thumbs_up
UNION ALL
SELECT 'favorites' AS table_name, COUNT(*) AS row_count FROM favorites
UNION ALL
SELECT 'follows' AS table_name, COUNT(*) AS row_count FROM follows
UNION ALL
SELECT 'comment' AS table_name, COUNT(*) AS row_count FROM comment;
