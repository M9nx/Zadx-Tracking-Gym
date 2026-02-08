-- ============================================================================
-- Fix Duplicate Users Between 'users' and 'regist' Tables
-- ============================================================================

-- STEP 1: Check for duplicate usernames
SELECT 'Checking for duplicate usernames...' AS status;
SELECT username FROM users WHERE username IN (SELECT username FROM regist);

-- STEP 2: Check for duplicate emails (users table only has emails)
SELECT 'Checking for duplicate emails in users table...' AS status;
SELECT email, COUNT(*) as count FROM users GROUP BY email HAVING count > 1;

-- STEP 3: Show all records with username 'admin3'
SELECT '--- Users table ---' AS table_name;
SELECT user_id, username, email, first_name, last_name, role FROM users WHERE username LIKE 'admin%';

SELECT '--- Regist table ---' AS table_name;
SELECT id, username, first_name, last_name, role FROM regist WHERE username LIKE 'admin%';

-- STEP 4: Clean up duplicates (UNCOMMENT TO EXECUTE)
-- Option A: Delete specific user from 'users' table
-- DELETE FROM users WHERE username = 'admin3';

-- Option B: Delete specific user from 'regist' table (legacy)
-- DELETE FROM regist WHERE username = 'admin3';

-- Option C: Delete all admins with specific email
-- DELETE FROM users WHERE email = 'flowmoner@gmail.com';

-- STEP 5: After cleanup, verify
SELECT '--- Final verification ---' AS status;
SELECT COUNT(*) as users_count FROM users;
SELECT COUNT(*) as regist_count FROM regist;

-- ============================================================================
-- RECOMMENDATION: Migrate all 'regist' users to 'users' table
-- ============================================================================

/*
-- Migration script to move regist users to users table:

INSERT INTO users (username, password, first_name, last_name, email, role, is_active, created_at)
SELECT 
    r.username,
    r.password,
    r.first_name,
    r.last_name,
    CONCAT(r.username, '@gym.zadx.local') AS email,  -- Generate fake email if missing
    r.role,
    TRUE as is_active,
    r.created_at
FROM regist r
WHERE r.username NOT IN (SELECT username FROM users);

-- After migration, you can drop the regist table:
-- DROP TABLE regist;
*/
