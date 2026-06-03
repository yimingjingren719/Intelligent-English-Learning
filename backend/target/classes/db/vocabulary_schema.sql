-- ============================================
-- 生词本表
-- ============================================
USE english_platform;

CREATE TABLE IF NOT EXISTS vocabulary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    word VARCHAR(100) NOT NULL COMMENT '单词',
    translation VARCHAR(500) COMMENT '中文释义',
    source_question_id BIGINT COMMENT '来源题目ID',
    review_count INT DEFAULT 0 COMMENT '复习次数',
    mastered TINYINT DEFAULT 0 COMMENT '0-未掌握 1-已掌握',
    last_review_time DATETIME COMMENT '最近复习时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_user_mastered (user_id, mastered),
    UNIQUE KEY uk_user_word (user_id, word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生词本';
