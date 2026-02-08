-- ============================================================================
-- QUICK DATABASE CHECK
-- Run this to see current state of email config and users
-- ============================================================================

USE gymm;

-- ============================================================================
-- 1. CHECK EMAIL CONFIGURATION
-- ============================================================================
SELECT '=== EMAIL CONFIGURATION ===' AS '';
SELECT setting_key, setting_value, description 
FROM system_settings 
WHERE setting_key LIKE 'email.%'
ORDER BY setting_key;

SELECT '' AS '';
SELECT CASE 
    WHEN COUNT(*) = 0 THEN '❌ NO EMAIL CONFIG FOUND - Run fix_email_and_regist.sql'
    WHEN COUNT(*) < 6 THEN '⚠️ INCOMPLETE EMAIL CONFIG - Missing some settings'
    ELSE '✅ Email configuration looks complete'
END AS 'Email Config Status'
FROM system_settings 
WHERE setting_key LIKE 'email.%';

-- ============================================================================
-- 2. CHECK REGIST TABLE STRUCTURE
-- ============================================================================
SELECT '' AS '';
SELECT '=== REGIST TABLE STRUCTURE ===' AS '';
SHOW COLUMNS FROM regist;

SELECT '' AS '';
SELECT CASE 
    WHEN COUNT(*) > 0 THEN '✅ Email column exists in regist table'
    ELSE '❌ Email column MISSING in regist table - Run fix_email_and_regist.sql'
END AS 'Regist Email Column Status'
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'gymm' 
AND TABLE_NAME = 'regist' 
AND COLUMN_NAME = 'email';

-- ============================================================================
-- 3. LIST ALL USERS IN USERS TABLE
-- ============================================================================
SELECT '' AS '';
SELECT '=== USERS TABLE (NEW SYSTEM) ===' AS '';
SELECT 
    user_id, 
    username, 
    first_name, 
    last_name, 
    email, 
    mobile, 
    role, 
    branch_id, 
    is_active,
    DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS created
FROM users
ORDER BY user_id DESC
LIMIT 10;

SELECT '' AS '';
SELECT CONCAT('Total users in users table: ', COUNT(*)) AS 'User Count'
FROM users;

-- ============================================================================
-- 4. LIST ALL USERS IN REGIST TABLE
-- ============================================================================
SELECT '' AS '';
SELECT '=== REGIST TABLE (OLD SYSTEM) ===' AS '';
SELECT 
    id, 
    first_name, 
    last_name, 
    username, 
    email,
    role,
    DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS created
FROM regist
ORDER BY id;

-- ============================================================================
-- 5. CHECK BRANCHES
-- ============================================================================
SELECT '' AS '';
SELECT '=== AVAILABLE BRANCHES ===' AS '';
SELECT branch_id, branch_name, address, phone 
FROM branches
ORDER BY branch_id;

-- ============================================================================
-- 6. SUMMARY
-- ============================================================================
SELECT '' AS '';
SELECT '=== SUMMARY ===' AS '';
SELECT 
    'Email Settings' AS 'Component',
    COUNT(*) AS 'Count',
    CASE 
        WHEN COUNT(*) >= 6 THEN '✅ OK'
        WHEN COUNT(*) > 0 THEN '⚠️ Incomplete'
        ELSE '❌ Missing'
    END AS 'Status'
FROM system_settings WHERE setting_key LIKE 'email.%'
UNION ALL
SELECT 
    'Users Table',
    COUNT(*),
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Has Users'
        ELSE '⚠️ Empty'
    END
FROM users
UNION ALL
SELECT 
    'Regist Table',
    COUNT(*),
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Has Users'
        ELSE '⚠️ Empty'
    END
FROM regist
UNION ALL
SELECT 
    'Branches',
    COUNT(*),
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Has Branches'
        ELSE '❌ No Branches'
    END
FROM branches;

