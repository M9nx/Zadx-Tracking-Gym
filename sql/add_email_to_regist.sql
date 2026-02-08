-- Add email field to regist table for admin notifications
ALTER TABLE regist 
ADD COLUMN email VARCHAR(255) AFTER last_name,
ADD INDEX idx_email (email);

-- Update existing records to have a default email
UPDATE regist 
SET email = CONCAT(username, '@gym.local')
WHERE email IS NULL OR email = '';
