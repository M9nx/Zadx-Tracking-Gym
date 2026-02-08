-- Update Database Structure for Add Member Feature
-- Run this script in MySQL Workbench or your MySQL client

USE gymm;

-- Drop the old table
DROP TABLE IF EXISTS add_member;

-- Create the new table with correct structure
CREATE TABLE add_member (
    ID_NUM VARCHAR(50) PRIMARY KEY,
    FIRST_NAME VARCHAR(100) NOT NULL,
    LAST_NAME VARCHAR(100) NOT NULL,
    MOBILE_NUM VARCHAR(20) NOT NULL,
    HEIGHT DECIMAL(5,2),
    WEIGHT DECIMAL(5,2),
    GENDER VARCHAR(10),
    PERIOD_INFO VARCHAR(255),
    PAYMENT DECIMAL(10,2),
    NUM_MEMB INT NOT NULL,
    START_DATE DATE,
    END_DATE DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data (optional)
INSERT INTO add_member (ID_NUM, FIRST_NAME, LAST_NAME, MOBILE_NUM, HEIGHT, WEIGHT, GENDER, PERIOD_INFO, PAYMENT, NUM_MEMB, START_DATE, END_DATE) 
VALUES 
('12345678', 'John', 'Doe', '0123456789', 180.0, 75.5, 'male', '30 days (2025-01-01 to 2025-01-31)', 500.00, 1, '2025-01-01', '2025-01-31'),
('87654321', 'Jane', 'Smith', '0987654321', 165.0, 65.0, 'female', '90 days (2025-01-01 to 2025-03-31)', 1200.00, 2, '2025-01-01', '2025-03-31');

-- Verify the table structure
DESCRIBE add_member;

-- Show success message
SELECT 'Database updated successfully!' AS Message;
