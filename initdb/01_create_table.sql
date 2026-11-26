CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    scheduled_date DATE NULL,
    planned_start_time TIME NULL,
    planned_end_time TIME NULL,
    planned_minutes INT NULL,
    actual_seconds INT NOT NULL DEFAULT 0,
    timer_started_at DATETIME NULL,
    timer_status VARCHAR(16) NOT NULL DEFAULT 'IDLE',
    project_id BIGINT NULL,
    section_id BIGINT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME NULL,
    deleted_at DATETIME NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category CHECK (
        category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')
    ),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3))
);

CREATE TABLE projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NULL,
    target_date DATE NULL,
    icon VARCHAR(16) NOT NULL DEFAULT '●',
    color VARCHAR(16) NOT NULL DEFAULT '#596579',
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE project_sections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_sections_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

ALTER TABLE todos
    ADD CONSTRAINT fk_todos_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_todos_section
        FOREIGN KEY (section_id) REFERENCES project_sections(id)
        ON DELETE SET NULL;

CREATE TABLE todo_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    todo_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NULL,
    file_size BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_todo_attachments_stored_filename (stored_filename),
    CONSTRAINT fk_todo_attachments_todo
        FOREIGN KEY (todo_id) REFERENCES todos(id)
        ON DELETE CASCADE
);

CREATE TABLE sprint (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_sprint_dates CHECK (start_date <= end_date)
);

INSERT INTO sprint (name, start_date, end_date)
VALUES ('Sprint 2026-11', '2026-11-01', '2026-11-30');

UPDATE todos
SET completed_at = '2026-04-05 00:00:00'
WHERE completed = TRUE
ORDER BY id
LIMIT 1;
