-- ============================================================================
-- FIX EMAIL CONFIGURATION AND REGIST TABLE
-- This script adds email column to regist table and sets up email config
-- ============================================================================

USE gymm;

-- ============================================================================
-- PART 1: Add email column to regist table (if not exists)
-- ============================================================================
SET @dbname = DATABASE();
SET @tablename = 'regist';
SET @columnname = 'email';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (TABLE_SCHEMA = @dbname)
     AND (TABLE_NAME = @tablename)
     AND (COLUMN_NAME = @columnname)) > 0,
  'SELECT ''Column already exists'' AS message;',
  'ALTER TABLE regist ADD COLUMN email VARCHAR(255) AFTER last_name, ADD INDEX idx_email (email);'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Update existing regist records to have default email
UPDATE regist 
SET email = CONCAT(username, '@gym.local')
WHERE email IS NULL OR email = '';

-- ============================================================================
-- PART 2: Setup Email Configuration in system_settings
-- ============================================================================

-- Delete existing email settings if any
DELETE FROM system_settings WHERE setting_key LIKE 'email.%';

-- Insert email configuration with Gmail App Password
INSERT INTO system_settings (setting_key, setting_value, description, updated_by) VALUES
('email.sender_address', 'gym.zadx@gmail.com', 'Email sender address for notifications', 1),
('email.smtp_host', 'smtp.gmail.com', 'SMTP server hostname', 1),
('email.smtp_port', '465', 'SMTP server port (SSL)', 1),
('email.smtp_username', 'gym.zadx@gmail.com', 'SMTP authentication username', 1),
('email.smtp_password', 'nylmcudjxzksxedy', 'SMTP authentication password (App Password)', 1),
('email.smtp_tls', 'true', 'Enable TLS/SSL encryption', 1);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Show regist table structure
DESCRIBE regist;

-- Show all regist users
SELECT id, first_name, last_name, username, email, role FROM regist;

-- Show email configuration
SELECT setting_key, setting_value, description 
FROM system_settings 
WHERE setting_key LIKE 'email.%'
ORDER BY setting_key;

-- Show all users in users table
SELECT user_id, username, first_name, last_name, email, mobile, role, branch_id, is_active 
FROM users
ORDER BY user_id;

SELECT '✓ Email configuration completed!' AS status;
