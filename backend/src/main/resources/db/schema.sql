CREATE TABLE IF NOT EXISTS knowledge_topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(128),
    overview TEXT,
    keywords VARCHAR(512),
    difficulty_level INT,
    tags VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS algorithm_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    name VARCHAR(255),
    core_idea TEXT,
    step_breakdown TEXT,
    time_complexity VARCHAR(64),
    space_complexity VARCHAR(64),
    code_snippet MEDIUMTEXT,
    visualization_hint TEXT,
    mermaid_code TEXT,
    animation_url VARCHAR(512),
    CONSTRAINT fk_detail_topic FOREIGN KEY (topic_id) REFERENCES knowledge_topic (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    content TEXT,
    keywords VARCHAR(255),
    embedding_json MEDIUMTEXT,
    FULLTEXT KEY idx_chunk_content (content),
    CONSTRAINT fk_chunk_topic FOREIGN KEY (topic_id) REFERENCES knowledge_topic (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS qa_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question TEXT,
    answer MEDIUMTEXT,
    reference_summary MEDIUMTEXT,
    latency_ms BIGINT,
    model VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO admin_user (username, password) VALUES ('admin', 'admin123') ON DUPLICATE KEY UPDATE username=username;

