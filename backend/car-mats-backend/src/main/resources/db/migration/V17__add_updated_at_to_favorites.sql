-- V17: Add updated_at column to favorites table

ALTER TABLE favorites ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
