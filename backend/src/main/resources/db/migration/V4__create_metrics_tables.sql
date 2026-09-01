-- Metrics Daily Rollup
CREATE TABLE IF NOT EXISTS metrics_daily (
                                             id BIGSERIAL PRIMARY KEY,
                                             metric_date DATE NOT NULL,
                                             user_id BIGINT REFERENCES users(id),
    department_id BIGINT REFERENCES departments(id),
    items_resolved INT DEFAULT 0,
    items_created INT DEFAULT 0,
    avg_resolution_hours NUMERIC(10,2),
    median_resolution_hours NUMERIC(10,2),
    first_response_hours NUMERIC(10,2),
    comments_posted INT DEFAULT 0,
    clarity_avg_score NUMERIC(5,2),
    csat_avg NUMERIC(3,2),
    kudos_received INT DEFAULT 0,
    off_hours_resolutions INT DEFAULT 0,
    UNIQUE(metric_date, user_id)
    );

-- Satisfaction Ratings
CREATE TABLE IF NOT EXISTS satisfaction_ratings (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    work_item_id BIGINT REFERENCES work_items(id),
    rated_by BIGINT REFERENCES users(id),
    resolved_by BIGINT REFERENCES users(id),
    stars SMALLINT CHECK (stars BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(work_item_id, rated_by)
    );

-- Kudos
CREATE TABLE IF NOT EXISTS kudos (
                                     id BIGSERIAL PRIMARY KEY,
                                     given_by BIGINT REFERENCES users(id),
    given_to BIGINT REFERENCES users(id),
    work_item_id BIGINT REFERENCES work_items(id) NULL,
    message TEXT,
    tag VARCHAR(40) NULL,
    created_at TIMESTAMPTZ DEFAULT now()
    );
