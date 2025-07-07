-- V1__Initial_Schema.sql

-- Alerts Table
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    type VARCHAR(255),
    severity VARCHAR(255),
    metrics_snapshot JSON, -- Changed from JSONB to JSON for H2 compatibility
    root_cause TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Alert Suggestions (Element Collection for Alert's suggestions list)
CREATE TABLE alert_suggestions (
    alert_id BIGINT NOT NULL,
    suggestion VARCHAR(1024), -- Increased length for suggestions
    CONSTRAINT fk_alert_suggestions_alert FOREIGN KEY (alert_id) REFERENCES alerts (id)
);

-- Actions Table
CREATE TABLE actions (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT,
    commit_hash VARCHAR(255),
    description TEXT,
    resolution_status VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_actions_alert FOREIGN KEY (alert_id) REFERENCES alerts (id)
);

-- Metrics Table
CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    namespace VARCHAR(255),
    service_name VARCHAR(255),
    service_version VARCHAR(255),
    deployment_id VARCHAR(255),
    tags JSON, -- Changed from JSONB to JSON
    value DOUBLE PRECISION,
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

-- Indexes for Metrics Table
CREATE INDEX idx_metric_name_timestamp ON metrics (name, timestamp);
CREATE INDEX idx_metric_timestamp ON metrics (timestamp);

-- Feedback Table
CREATE TABLE feedback (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT,
    user_id VARCHAR(255),
    feedback_type VARCHAR(255),
    comments TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_feedback_alert FOREIGN KEY (alert_id) REFERENCES alerts (id)
);

-- Knowledge Base Entries Table
CREATE TABLE knowledge_base_entries (
    id BIGSERIAL PRIMARY KEY,
    pattern_name VARCHAR(255),
    description TEXT,
    conditions JSON, -- Changed from JSONB to JSON
    correlations JSON, -- Changed from JSONB to JSON
    suggested_actions JSON, -- Changed from JSONB to JSON
    effectiveness_score DOUBLE PRECISION,
    usage_count INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Flyway schema history table will be created automatically by Flyway
-- Name: flyway_schema_history
-- Contains: installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success
