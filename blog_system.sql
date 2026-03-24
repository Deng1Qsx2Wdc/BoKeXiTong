/*
 Navicat Premium Dump SQL

 Source Server         : 数据库
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : blog_system

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 26/01/2026 16:25:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录令牌',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$zu/y4bExPPEZCaSfwK8wcemSFtiBL72DwFJZIFd7qWVhX9Vvou5lS', '1234567890');

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文章正文',
  `author_id` bigint NULL DEFAULT NULL COMMENT '作者ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `status` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '状态',
  `thumbs_up` bigint NULL DEFAULT NULL,
  `favorites` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE COMMENT '分类索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article
-- ----------------------------
INSERT INTO `article` VALUES (2012509915972300802, 2011717000710721537, '原神', '原神', 1, '2026-01-17 20:58:49', NULL, NULL, NULL, NULL);
INSERT INTO `article` VALUES (2012510141772656642, 2011717000710721537, '千星奇域', '千星奇域', 2012934145, '2026-01-17 20:59:43', NULL, NULL, NULL, NULL);
INSERT INTO `article` VALUES (2015370072267878402, 2015352555227000800, 'aaxascas ', '阿思达时大时小', 2015348121642094594, '2026-01-25 18:24:04', NULL, '0', NULL, NULL);
INSERT INTO `article` VALUES (2015370148134449153, 2011723383770382300, '撒擦拭吃按时吃啊', '啊啥上次按时吃爱上', 2015348121642094594, '2026-01-25 18:24:22', NULL, '0', NULL, NULL);
INSERT INTO `article` VALUES (2015406227445157890, 2011723316116258800, '你好', '你好', 2015348121642094594, '2026-01-25 20:47:44', NULL, '0', NULL, NULL);
INSERT INTO `article` VALUES (2015407200070701058, 2011723383770382300, '安师大', '阿萨重新', 2015348121642094594, '2026-01-25 20:51:36', NULL, '0', NULL, NULL);
INSERT INTO `article` VALUES (2015407332421963778, 2015352555227000800, '啊啥', '按时吃爱上', 2015348121642094594, '2026-01-25 20:52:07', NULL, '0', NULL, NULL);
INSERT INTO `article` VALUES (2015408436857741313, 2011717000710721500, '撒打算', '啊啥上次', 2015348121642094594, '2026-01-25 20:56:30', NULL, '0', NULL, NULL);
INSERT INTO `article` VALUES (2015425920595816450, 2011717000710721500, '这是初中生', '撒擦拭 AC撒手', 2015348121642094594, '2026-01-25 22:05:59', NULL, '0', NULL, NULL);

-- ----------------------------
-- Table structure for author
-- ----------------------------
DROP TABLE IF EXISTS `author`;
CREATE TABLE `author`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2015348121642094595 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of author
-- ----------------------------
INSERT INTO `author` VALUES (-184881150, 'User', '1qsx2wdc');
INSERT INTO `author` VALUES (1, 'User', '1qsx2wdc');
INSERT INTO `author` VALUES (2012934145, 'author', '1');
INSERT INTO `author` VALUES (2015344977939943425, '顺风车啊', '$2a$10$tvjwSzlcjDYuMx8f02xXAuZ.thkb482mTFWjbL8ptTFTBFmJlmS42');
INSERT INTO `author` VALUES (2015348121642094594, 'admin', '$2a$10$zu/y4bExPPEZCaSfwK8wcemSFtiBL72DwFJZIFd7qWVhX9Vvou5lS');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (2011717000710721537, '游戏');
INSERT INTO `category` VALUES (2011721901545291777, '小说');
INSERT INTO `category` VALUES (2011723316116258818, '娱乐');
INSERT INTO `category` VALUES (2011723341760233473, '视频');
INSERT INTO `category` VALUES (2011723383770382338, '赛事');
INSERT INTO `category` VALUES (2015352555227000833, '建筑');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL,
  `article_id` bigint NULL DEFAULT NULL,
  `author_id` bigint NULL DEFAULT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL,
  `author_id` bigint NULL DEFAULT NULL,
  `article_id` bigint NULL DEFAULT NULL,
  `favorites_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------

-- ----------------------------
-- Table structure for follows
-- ----------------------------
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows`  (
  `id` bigint NOT NULL,
  `author_id` bigint NOT NULL COMMENT '关注者ID',
  `target_id` bigint NOT NULL COMMENT '被关注者id',
  `follow_time` timestamp NULL DEFAULT NULL COMMENT '关注时间',
  `status` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_author_target`(`author_id` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follows
-- ----------------------------

-- ----------------------------
-- Table structure for thumbs_up
-- ----------------------------
DROP TABLE IF EXISTS `thumbs_up`;
CREATE TABLE `thumbs_up`  (
  `id` int NOT NULL,
  `author_id` bigint NULL DEFAULT NULL,
  `article_id` bigint NULL DEFAULT NULL,
  `thumbs_up_time` timestamp NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id` DESC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of thumbs_up
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
