-- liquibase formatted sql

--changeset vcharushnikov:1

CREATE TABLE notification_task
(
    id BIGINT PRIMARY KEY,
    chat_id BIGINT,
    notification TEXT,
    date_time TIMESTAMP
);