-- V12: Add updated_at column to order_status_history table for BaseEntity compliance

ALTER TABLE order_status_history
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
