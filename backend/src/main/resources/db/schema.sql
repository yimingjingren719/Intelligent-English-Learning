-- ============================================
-- 个性化英语学习平台 - 数据库初始化脚本
-- Database: MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS english_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE english_platform;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT/ADMIN',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-正常 1-禁用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 题库表
-- ============================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    question_type VARCHAR(30) NOT NULL COMMENT '题目类型: SINGLE_CHOICE/TRUE_FALSE/FILL_BLANK/READING',
    content TEXT NOT NULL COMMENT '题目内容(富文本HTML)',
    options JSON COMMENT '选项: [{"label":"A","text":"..."}]',
    correct_answer VARCHAR(500) NOT NULL COMMENT '正确答案',
    analysis TEXT COMMENT '答案解析',
    difficulty INT NOT NULL DEFAULT 5 COMMENT '难度等级: 1-10',
    tags VARCHAR(500) COMMENT '知识点标签, 逗号分隔',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-启用 1-禁用',
    creator_id BIGINT COMMENT '创建者ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_question_type (question_type),
    KEY idx_difficulty (difficulty),
    KEY idx_creator (creator_id),
    KEY idx_tags (tags(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- ============================================
-- 3. 作答记录表
-- ============================================
CREATE TABLE IF NOT EXISTS answer_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    session_id VARCHAR(50) COMMENT '测试会话ID',
    user_answer VARCHAR(500) COMMENT '用户答案',
    is_correct TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确: 0-错误 1-正确',
    time_taken INT COMMENT '答题用时(秒)',
    ai_feedback TEXT COMMENT 'AI评价(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_question_id (question_id),
    KEY idx_session_id (session_id),
    KEY idx_user_question (user_id, question_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作答记录表';

-- ============================================
-- 4. 错题记录表
-- ============================================
CREATE TABLE IF NOT EXISTS error_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    error_count INT NOT NULL DEFAULT 1 COMMENT '错误次数',
    last_wrong_answer VARCHAR(500) COMMENT '最近错误答案',
    mastered TINYINT NOT NULL DEFAULT 0 COMMENT '是否已掌握: 0-未掌握 1-已掌握',
    last_practice_time DATETIME COMMENT '最后练习时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_question (user_id, question_id),
    KEY idx_user_mastered (user_id, mastered),
    KEY idx_last_practice (last_practice_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题记录表';

-- ============================================
-- 5. 学习记录表
-- ============================================
CREATE TABLE IF NOT EXISTS learning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_type VARCHAR(30) NOT NULL COMMENT '记录类型: TEST/PRACTICE/AI_QA',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '详细内容(JSON)',
    score INT COMMENT '得分',
    total_questions INT COMMENT '总题数',
    correct_count INT COMMENT '正确数',
    time_taken INT COMMENT '用时(秒)',
    tags VARCHAR(500) COMMENT '涉及知识点',
    ai_analysis TEXT COMMENT 'AI分析结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_record_type (record_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- ============================================
-- 初始化数据：管理员账号
-- 密码: admin123 (MD5加密)
-- ============================================
INSERT INTO users (username, password, nickname, role, status) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 'ADMIN', 0);

-- ============================================
-- 初始化数据：示例题目
-- ============================================
INSERT INTO questions (question_type, content, options, correct_answer, analysis, difficulty, tags) VALUES
('SINGLE_CHOICE', 'She _____ to school by bus every day.',
 '[{"label":"A","text":"go"},{"label":"B","text":"goes"},{"label":"C","text":"going"},{"label":"D","text":"gone"}]',
 'B', '主语She是第三人称单数，一般现在时动词需加-s/es，go的第三人称单数形式是goes。', 2, '语法,一般现在时'),
('SINGLE_CHOICE', 'I have been learning English _____ five years.',
 '[{"label":"A","text":"since"},{"label":"B","text":"for"},{"label":"C","text":"from"},{"label":"D","text":"during"}]',
 'B', '"for + 时间段"表示持续的时间长度，five years是一个时间段，用for。"since + 时间点"。', 3, '语法,现在完成时,介词'),
('TRUE_FALSE', 'The word "beautiful" is an adverb.', NULL, 'FALSE',
 '"Beautiful"是形容词(adjective)，其副词形式是"beautifully"。', 2, '词性,形容词,副词'),
('SINGLE_CHOICE', 'Which of the following sentences is grammatically correct?',
 '[{"label":"A","text":"He don''t like coffee."},{"label":"B","text":"He doesn''t likes coffee."},{"label":"C","text":"He doesn''t like coffee."},{"label":"D","text":"He not like coffee."}]',
 'C', '一般现在时第三人称单数否定句结构：主语 + doesn''t + 动词原形。A缺第三人称单数，B多加了-s，D缺少助动词。', 3, '语法,否定句,一般现在时'),
('SINGLE_CHOICE', 'If I _____ you, I would accept the offer.',
 '[{"label":"A","text":"am"},{"label":"B","text":"was"},{"label":"C","text":"were"},{"label":"D","text":"be"}]',
 'C', '虚拟语气中，与现在事实相反的假设，be动词一律用were，这是固定用法。', 5, '语法,虚拟语气'),
('TRUE_FALSE', 'A semicolon can be used to join two independent clauses.', NULL, 'TRUE',
 '分号(;)可以用来连接两个独立分句，表示它们之间的紧密联系。例如: "I went to the store; it was closed."', 6, '标点,写作'),
('SINGLE_CHOICE', 'The book _____ on the desk is mine.',
 '[{"label":"A","text":"lay"},{"label":"B","text":"lying"},{"label":"C","text":"lied"},{"label":"D","text":"lies"}]',
 'B', '此处需要现在分词作后置定语修饰the book。lying是lie(躺/位于)的现在分词形式。', 5, '语法,非谓语动词,分词'),
('SINGLE_CHOICE', 'Not until he arrived at the station _____ that he had left his ticket at home.',
 '[{"label":"A","text":"he realized"},{"label":"B","text":"did he realize"},{"label":"C","text":"he had realized"},{"label":"D","text":"realized he"}]',
 'B', 'Not until置于句首时，主句需部分倒装：助动词 + 主语 + 动词原形。', 7, '语法,倒装句');
