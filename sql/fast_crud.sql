/*
 Navicat Premium Dump SQL

 Source Server         : bd
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : fast_crud

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 15/01/2026 10:59:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_field_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_field_config`;
CREATE TABLE `sys_field_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联的物理表名 (如 tb_cs)',
  `prop` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段名 (如 age)',
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '显示名 (如 年龄)',
  `ui_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Input' COMMENT '组件类型 (Input, Select, DatePicker)',
  `options` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下拉选项 (男,女)',
  `is_required` tinyint(1) NULL DEFAULT 0,
  `is_show_in_list` tinyint(1) NULL DEFAULT 1 COMMENT '是否在列表显示',
  `sort_order` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_table_name`(`table_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段元数据配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_field_config
-- ----------------------------
INSERT INTO `sys_field_config` VALUES (1, 'tb_paper', 'col_title', '论文名称', 'Input', '', 0, 1, 0);
INSERT INTO `sys_field_config` VALUES (2, 'tb_paper', 'col_type', '论文类型', 'Select', 'A1,B1,C1', 0, 1, 0);
INSERT INTO `sys_field_config` VALUES (3, 'tb_paper', 'col_crtertime', '论文创建时间', 'DatePicker', 'A1,B1,C1', 0, 1, 0);
INSERT INTO `sys_field_config` VALUES (4, 'tb_paper', 'col_crtername', '创建人', 'Input', '', 0, 1, 50);
INSERT INTO `sys_field_config` VALUES (5, 'tb_paper', 'col_count', '论文字数', 'InputNumber', '', 0, 1, 50);
INSERT INTO `sys_field_config` VALUES (6, 'tb_paper', 'col_sh', '审核', 'Switch', '', 0, 1, 50);
INSERT INTO `sys_field_config` VALUES (7, 'tb_paper', 'col_jf', '论文经费', 'Currency', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (8, 'tb_paper', 'col_files', '附件', 'Attachment', '.xlsx,.pdf,.png,.jpg', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (9, 'tb_zz', 'col_name', '名称', 'Input', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (10, 'tb_zz', 'col_size', '字数', 'InputNumber', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (11, 'tb_kn', 'col_no', '房间号', 'Input', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (12, 'tb_kn', 'col_fz', '房租', 'Currency', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (13, 'tb_kn', 'col_fx', 'f房型', 'Input', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (14, 'tb_kn', 'col_image', '房间图片', 'Attachment', '', 0, 1, 99);
INSERT INTO `sys_field_config` VALUES (15, 'tb_kn', 'col_zt', '状态', 'Switch', '', 0, 1, 99);

-- ----------------------------
-- Table structure for sys_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_module`;
CREATE TABLE `sys_module`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '显示名称 (如: 客户管理)',
  `table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库真实表名 (如: tb_customer)',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'List' COMMENT '图标名称 (对应 ElementPlus 图标)',
  `theme_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#e6f7ff' COMMENT '图标背景色/主题色',
  `record_count` int(11) NULL DEFAULT 0 COMMENT '数据行数缓存 (避免每次 count(*))',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序权重',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '软删除标记 (0:正常 1:删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_name`(`table_name` ASC) USING BTREE COMMENT '表名必须唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统模块/数据表配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_module
-- ----------------------------
INSERT INTO `sys_module` VALUES (1, 'Customer / 客户', 'tb_customer', 'User', '#e6f7ff', 1280, 0, NULL, '2025-12-02 12:12:10', '2025-12-02 10:12:10', 0);
INSERT INTO `sys_module` VALUES (2, 'Inventory / 库存', 'tb_inventory', 'Goods', '#f6ffed', 8450, 0, NULL, '2025-12-02 12:12:10', '2025-12-01 12:12:10', 0);
INSERT INTO `sys_module` VALUES (3, 'Projects / 项目', 'tb_project', 'Briefcase', '#fff7e6', 120, 0, NULL, '2025-12-02 12:12:10', '2025-12-02 14:41:56', 0);
INSERT INTO `sys_module` VALUES (4, 'Logs / 日志', 'tb_sys_log', 'Files', '#f9f0ff', 15400, 0, NULL, '2025-12-02 12:12:10', '2025-12-02 12:07:10', 0);
INSERT INTO `sys_module` VALUES (5, ' 订单', 'tb_order', 'List', '#e6f7ff', 3200, 0, NULL, '2025-12-02 12:12:10', NULL, 0);
INSERT INTO `sys_module` VALUES (6, '信息1', 'tb_info', 'User', '#e6f7ff', 0, 0, NULL, NULL, NULL, 0);
INSERT INTO `sys_module` VALUES (7, '测试', '测试', 'User', 'rgb(86, 19, 19)', 0, 1, '测试', NULL, NULL, 0);
INSERT INTO `sys_module` VALUES (9, 'cs', 'tb_cs', 'List', '#e6f7ff', 0, 0, NULL, NULL, '2025-12-02 15:14:30', 1);
INSERT INTO `sys_module` VALUES (10, '论文模块', 'tb_paper', 'List', '#e6f7ff', 0, 0, NULL, NULL, NULL, 0);
INSERT INTO `sys_module` VALUES (11, '专著', 'tb_zz', 'Goods', '#e6f7ff', 0, 0, NULL, NULL, NULL, 0);
INSERT INTO `sys_module` VALUES (12, '康宁大厦', 'tb_kn', 'List', '#e6f7ff', 0, 0, NULL, NULL, '2025-12-08 22:12:19', 0);

-- ----------------------------
-- Table structure for tb_cs
-- ----------------------------
DROP TABLE IF EXISTS `tb_cs`;
CREATE TABLE `tb_cs`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Fixed;

-- ----------------------------
-- Records of tb_cs
-- ----------------------------

-- ----------------------------
-- Table structure for tb_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_info`;
CREATE TABLE `tb_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Fixed;

-- ----------------------------
-- Records of tb_info
-- ----------------------------

-- ----------------------------
-- Table structure for tb_kn
-- ----------------------------
DROP TABLE IF EXISTS `tb_kn`;
CREATE TABLE `tb_kn`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `col_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `col_fz` decimal(10, 2) NULL DEFAULT 0.00,
  `col_fx` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `col_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `col_zt` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_kn
-- ----------------------------
INSERT INTO `tb_kn` VALUES (1, '2025-12-08 22:09:04', '2025-12-08 22:10:40', 'a301', 18000.00, '三四一厅', NULL, 1);

-- ----------------------------
-- Table structure for tb_paper
-- ----------------------------
DROP TABLE IF EXISTS `tb_paper`;
CREATE TABLE `tb_paper`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `col_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `col_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `col_crtertime` datetime NULL DEFAULT NULL,
  `col_crtername` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `col_count` int(11) NULL DEFAULT 0,
  `col_sh` tinyint(1) NULL DEFAULT 0,
  `col_jf` decimal(10, 2) NULL DEFAULT 0.00,
  `col_files` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_paper
-- ----------------------------
INSERT INTO `tb_paper` VALUES (1, '2025-12-03 14:19:10', '2025-12-03 22:32:14', '测试1', 'A1', '2025-12-18 00:00:00', 'xixia', 10000, 0, 3000.00, '857f03fd-b342-4917-bbc4-73517a6195f5.jpg');
INSERT INTO `tb_paper` VALUES (2, '2025-12-03 14:26:04', '2025-12-03 18:17:04', '测试2', 'C1', '2025-12-24 00:00:00', 'xixia', 100000, 0, 3000.00, 'fd823089-9f73-4f16-813c-a24ca6133630.xlsx');
INSERT INTO `tb_paper` VALUES (3, '2025-12-03 17:54:11', '2025-12-03 17:54:11', '1', 'B1', '2025-12-11 00:00:00', '11', 111, 1, 111.00, NULL);
INSERT INTO `tb_paper` VALUES (4, '2025-12-03 17:54:21', '2025-12-03 17:54:21', '11', 'C1', NULL, '111', 11, 1, 111.00, NULL);
INSERT INTO `tb_paper` VALUES (5, '2025-12-03 17:54:31', '2025-12-03 17:54:31', '1', '1', '2025-12-18 00:00:00', '11', 11, 0, 11.00, NULL);
INSERT INTO `tb_paper` VALUES (6, '2025-12-03 17:54:43', '2025-12-03 17:54:43', '1', 'B1', '2025-12-19 00:00:00', '11', 11, 0, 11.00, NULL);
INSERT INTO `tb_paper` VALUES (7, '2025-12-03 17:54:50', '2025-12-03 17:54:50', '11', 'B1', '2025-12-15 00:00:00', '111', 111, 0, 11.00, NULL);
INSERT INTO `tb_paper` VALUES (8, '2025-12-03 17:54:57', '2025-12-03 17:54:57', '111', 'C1', '2025-12-19 00:00:00', '111', 111, 0, 111.00, NULL);
INSERT INTO `tb_paper` VALUES (9, '2025-12-03 17:55:42', '2025-12-03 17:55:42', '1', 'B1', NULL, '11', 11, 0, 11.00, NULL);
INSERT INTO `tb_paper` VALUES (10, '2025-12-03 17:55:44', '2025-12-03 17:55:44', '11', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (11, '2025-12-03 17:55:47', '2025-12-03 17:55:47', '11', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (12, '2025-12-03 17:55:49', '2025-12-03 17:55:49', '11', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (15, '2025-12-03 17:55:56', '2025-12-03 17:55:56', '111', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (16, '2025-12-03 17:56:00', '2025-12-03 17:56:00', '11', 'A1', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (17, '2025-12-03 17:56:04', '2025-12-03 17:56:04', '11', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (18, '2025-12-03 17:56:06', '2025-12-03 17:56:06', '111', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (19, '2025-12-03 17:56:08', '2025-12-03 17:56:08', '111', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (20, '2025-12-03 17:56:10', '2025-12-03 17:56:10', '111', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_paper` VALUES (21, '2025-12-03 17:56:12', '2025-12-03 17:56:12', '111', NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for tb_zz
-- ----------------------------
DROP TABLE IF EXISTS `tb_zz`;
CREATE TABLE `tb_zz`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `col_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `col_size` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_zz
-- ----------------------------

-- ----------------------------
-- Table structure for ui_table
-- ----------------------------
DROP TABLE IF EXISTS `ui_table`;
CREATE TABLE `ui_table`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ui_id` bigint(20) NULL DEFAULT NULL COMMENT '界面id',
  `ui_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '界面名称',
  `table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '界面数据库表名',
  `ui_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '界面描述',
  `create_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建方式',
  `excelfile_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'excel文件名称',
  `excelfile_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'excel文件路径',
  `ui_status` tinyint(4) NULL DEFAULT NULL COMMENT '状态：0-草稿 1-已启用 2-已停用',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ui_table
-- ----------------------------
INSERT INTO `ui_table` VALUES (32, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (31, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (30, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (29, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (28, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (27, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (26, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (25, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (24, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');
INSERT INTO `ui_table` VALUES (23, 1, '测试', '测试', '测试', '测试', '测试', '测试', 1, '测试', '测试', '2025-11-28 17:40:52', '2025-11-28 17:40:52');

-- ----------------------------
-- Table structure for 测试
-- ----------------------------
DROP TABLE IF EXISTS `测试`;
CREATE TABLE `测试`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Fixed;

-- ----------------------------
-- Records of 测试
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
