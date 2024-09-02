-- Creates the sum_table if it doesn't exist
CREATE TABLE IF NOT EXISTS sum_table (
    key VARCHAR(255) PRIMARY KEY,
    sum BIGINT
);