-- Quick diagnostic - Run this first to see what's blocking the creation

-- Check for admin3 username
SELECT 'admin3 in users table:' AS check_type, user_id, username, email, role FROM users WHERE username = 'admin3'
UNION ALL
SELECT 'admin3 in regist table:' AS check_type, id, username, 'N/A' as email, role FROM regist WHERE username = 'admin3';

-- Check for flowmoner@gmail.com email
SELECT 'flowmoner email in users table:' AS check_type, user_id, username, email, role FROM users WHERE email = 'flowmoner@gmail.com';

-- Show all admins
SELECT '--- All admins in users table ---' AS status;
SELECT user_id, username, email, first_name, last_name, role, is_active FROM users WHERE username LIKE '%admin%' OR role = 'admin';

SELECT '--- All admins in regist table ---' AS status;
SELECT id, username, first_name, last_name, role FROM regist WHERE username LIKE '%admin%' OR role = 'admin';

-- SOLUTION: Delete the conflicting user (choose one)

-- Option 1: Delete admin3 from users table
-- DELETE FROM users WHERE username = 'admin3';

-- Option 2: Delete flowmoner email from users table
-- DELETE FROM users WHERE email = 'flowmoner@gmail.com';

-- Option 3: Delete all and start fresh
-- DELETE FROM users WHERE username LIKE 'admin%' AND role = 'admin';

-- Verify after deletion
-- SELECT COUNT(*) as remaining_users FROM users;
