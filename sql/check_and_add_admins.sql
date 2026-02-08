-- Check and add admin accounts for testing
-- Run this in MySQL Workbench

USE gymm;

-- Check existing users
SELECT 'Existing users:' AS info;
SELECT user_id, username, role, first_name, last_name, branch_id, is_active FROM users;

-- Check existing branches
SELECT 'Existing branches:' AS info;
SELECT branch_id, branch_name, address FROM branches;

-- If no admin accounts exist, insert them
-- Note: These passwords are BCrypt hashed for "admin123"

-- Check if admin_cairo exists
SELECT COUNT(*) AS admin_cairo_exists FROM users WHERE username = 'admin_cairo';

-- If admin_cairo doesn't exist, insert it
INSERT IGNORE INTO users (username, password, first_name, last_name, email, mobile, role, branch_id, is_active) 
VALUES ('admin_cairo', '$2a$12$wlC0H3VYPdZ5e.oKpFvKQ.IJC7XmFf7ZzXLyuGBPQy1EBcBm5Jb3C',
 'Ahmed', 'Hassan', 'admin.cairo@gymsystem.local', '+201111111111', 'admin', 1, true);

-- Check if admin_benha exists
SELECT COUNT(*) AS admin_benha_exists FROM users WHERE username = 'admin_benha';

-- If admin_benha doesn't exist, insert it
INSERT IGNORE INTO users (username, password, first_name, last_name, email, mobile, role, branch_id, is_active) 
VALUES ('admin_benha', '$2a$12$wlC0H3VYPdZ5e.oKpFvKQ.IJC7XmFf7ZzXLyuGBPQy1EBcBm5Jb3C',
 'Mohamed', 'Ali', 'admin.benha@gymsystem.local', '+201222222222', 'admin', 2, true);

-- Verify admin accounts
SELECT 'Admin accounts after insert:' AS info;
SELECT user_id, username, role, first_name, last_name, email, branch_id, is_active FROM users WHERE role = 'admin';

-- Test password verification (should show the hash)
SELECT 'Password hash for admin_cairo:' AS info;
SELECT username, password FROM users WHERE username = 'admin_cairo';
