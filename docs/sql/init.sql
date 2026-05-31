-- =====================================================
-- StatHarbor 数据库建表语句
-- 数据库: MySQL 8.0+
-- =====================================================

-- 通用数据记录表
CREATE TABLE `universal_data` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `project_key`   VARCHAR(255)    NOT NULL                 COMMENT '统计项目标识',
    `category`      VARCHAR(255)    NOT NULL                 COMMENT '数据分类',
    `version`       VARCHAR(64)     NOT NULL                 COMMENT '数据结构版本',
    `source`        VARCHAR(64)     NOT NULL                 COMMENT '数据来源，如 ocr/log/api/manual',
    `captured_at`   DATETIME        DEFAULT NULL             COMMENT '采集时间',
    `received_at`   DATETIME        DEFAULT NULL             COMMENT '后端收到时间',
    `note`          TEXT            DEFAULT NULL             COMMENT '备注',
    `payload`       JSON            DEFAULT NULL             COMMENT '原始JSON内容',
    PRIMARY KEY (`id`),
    INDEX `idx_project_key` (`project_key`),
    INDEX `idx_received_at` (`received_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用数据记录表';


-- 项目凭证表
CREATE TABLE `project_credential` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `project_key`   VARCHAR(255)    NOT NULL                 COMMENT '统计项目标识',
    `secret_key`    VARCHAR(255)    NOT NULL                 COMMENT '项目凭证密钥，用于API认证',
    `note`          TEXT            DEFAULT NULL             COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_project_key` (`project_key`),
    UNIQUE INDEX `uk_secret_key` (`secret_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目凭证表';
