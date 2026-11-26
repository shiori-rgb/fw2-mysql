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
    schedule_change_count INT NOT NULL DEFAULT 0,
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

CREATE TABLE app_settings (
    id BIGINT NOT NULL,
    initial_screen VARCHAR(32) NOT NULL DEFAULT '/',
    default_work_minutes INT NOT NULL DEFAULT 30,
    default_priority INT NOT NULL DEFAULT 2,
    week_start VARCHAR(16) NOT NULL DEFAULT 'monday',
    calendar_start TIME NOT NULL DEFAULT '08:00:00',
    calendar_end TIME NOT NULL DEFAULT '20:00:00',
    holidays_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    task_start_notice_minutes INT NOT NULL DEFAULT 10,
    due_notice_minutes INT NOT NULL DEFAULT 60,
    quiet_start TIME NOT NULL DEFAULT '22:00:00',
    quiet_end TIME NOT NULL DEFAULT '07:00:00',
    notification_sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);
INSERT INTO app_settings (id) VALUES (1);

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

CREATE TABLE todo_schedule_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    todo_id BIGINT NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_scheduled_date DATE NULL,
    new_scheduled_date DATE NULL,
    old_due_date DATE NULL,
    new_due_date DATE NULL,
    old_planned_start_time TIME NULL,
    new_planned_start_time TIME NULL,
    old_planned_end_time TIME NULL,
    new_planned_end_time TIME NULL,
    old_planned_minutes INT NULL,
    new_planned_minutes INT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_history_todo FOREIGN KEY (todo_id) REFERENCES todos(id) ON DELETE CASCADE
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
