-- ============================================================================
-- GYM MANAGEMENT SYSTEM - COMPLETE DATABASE SCHEMA WITH SEED DATA
-- ============================================================================
-- Database: gymm
-- MySQL Version: 8.0+
-- Credentials: root / root (DEVELOPMENT ONLY - Change in production!)
-- Instructions: Run this file in MySQL Workbench or command line
-- Command: mysql -u root -p < schema_and_seed.sql
-- ============================================================================

-- Drop database if exists (WARNING: This will delete all data!)
-- Comment out the next line if you want to preserve existing data
DROP DATABASE IF EXISTS gymm;

-- Create database
CREATE DATABASE IF NOT EXISTS gymm
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE gymm;

-- ============================================================================
-- TABLE: branches
-- Stores gym branch locations
-- ============================================================================
CREATE TABLE branches (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_branch_name (branch_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: users
-- Stores all system users (owner, admin, coach)
-- Password storage: Use BCrypt or PBKDF2 hashing (see PasswordUtil.java)
-- ============================================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    -- Password should be hashed using BCrypt (60 chars) or PBKDF2 (longer)
    -- NEVER store plain text passwords!
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(20) NULL,
    role ENUM('owner', 'admin', 'coach') NOT NULL,
    -- branch_id is NULL for owner (system-wide access)
    branch_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE SET NULL,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_branch_role (branch_id, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: members
-- Stores gym member information
-- ============================================================================
CREATE TABLE members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    -- Random 8-digit internal ID for member cards/reference
    random_id INT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    height DECIMAL(5,2) COMMENT 'Height in cm',
    weight DECIMAL(5,2) COMMENT 'Weight in kg',
    gender ENUM('male', 'female', 'other') NOT NULL,
    date_of_birth DATE,
    payment DECIMAL(10,2) NOT NULL COMMENT 'Payment amount',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    period VARCHAR(50) NOT NULL COMMENT 'e.g., "30 days", "3 months"',
    assigned_branch INT NOT NULL,
    assigned_coach INT NULL COMMENT 'Coach responsible for this member',
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_branch) REFERENCES branches(branch_id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_coach) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_mobile (mobile),
    INDEX idx_random_id (random_id),
    INDEX idx_branch (assigned_branch),
    INDEX idx_coach (assigned_coach),
    INDEX idx_active (is_active),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: training_progress
-- Tracks training progress notes for members by coaches
-- ============================================================================
CREATE TABLE training_progress (
    progress_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    coach_id INT NOT NULL,
    session_date DATE NOT NULL,
    notes TEXT NOT NULL COMMENT 'Training notes, exercises, progress',
    rating INT CHECK (rating BETWEEN 1 AND 5) COMMENT 'Session rating 1-5',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_member (member_id),
    INDEX idx_coach (coach_id),
    INDEX idx_session_date (session_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: audit_logs
-- Tracks all sensitive system actions for security and compliance
-- ============================================================================
CREATE TABLE audit_logs (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL COMMENT 'User who performed the action (NULL for system events)',
    action VARCHAR(100) NOT NULL COMMENT 'Action type: LOGIN_SUCCESS, LOGIN_FAIL, PASSWORD_RESET, etc.',
    target_id INT NULL COMMENT 'ID of affected entity (user_id, member_id, etc.)',
    target_type VARCHAR(50) NULL COMMENT 'Type of target: USER, MEMBER, BRANCH, etc.',
    details TEXT COMMENT 'Additional details in JSON or plain text',
    ip_address VARCHAR(45) COMMENT 'IP address of user',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_action (user_id, action),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: system_settings
-- Stores system-wide configuration settings
-- ============================================================================
CREATE TABLE system_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_by INT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: regist (LEGACY SUPPORT)
-- DEPRECATED: This table supports the old LOGIN.java interface
-- For new development, use the 'users' table and LoginView.java
-- ============================================================================
CREATE TABLE regist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'Store hashed passwords only!',
    role ENUM('owner', 'admin', 'coach') DEFAULT 'admin' COMMENT 'User role for dashboard routing',
    security_question VARCHAR(255),
    security_answer VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SEED DATA: Default Branches
-- ============================================================================
INSERT INTO branches (branch_name, address, phone) VALUES
('Cairo', '123 Tahrir Square, Cairo, Egypt', '+20-2-1234-5678'),
('Benha', '456 University Street, Benha, Egypt', '+20-13-987-6543');

-- ============================================================================
-- SEED DATA: System Users
-- ============================================================================
-- IMPORTANT: Passwords shown here are PLACEHOLDERS for hashed passwords
-- In production, these MUST be replaced with proper BCrypt/PBKDF2 hashes
-- 
-- TO GENERATE HASHED PASSWORD:
-- 1. Use the PasswordUtil.hashPassword() method in your Java code
-- 2. Run: System.out.println(PasswordUtil.hashPassword("your_password"));
-- 3. Replace the placeholder below with the actual hash
--
-- Current password hashes are for: "owner123", "admin123", "coach123"
-- These are BCrypt hashes generated with cost factor 12
-- ============================================================================

-- OWNER user (system-wide access, no branch assignment)
-- Email: m9nx11@gmail.com
-- Password: owner123 (CHANGE THIS IN PRODUCTION!)
-- Hash below is BCrypt of "owner123"
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('owner', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYPZKbNKJZe', 
 'System', 'Owner', 'm9nx11@gmail.com', '+201234567890', 'owner', NULL);

-- ADMIN users (one for each branch)
-- Cairo Admin
-- Username: admin_cairo, Password: admin123
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('admin_cairo', '$2a$12$wlC0H3VYPdZ5e.oKpFvKQ.IJC7XmFf7ZzXLyuGBPQy1EBcBm5Jb3C',
 'Ahmed', 'Hassan', 'admin.cairo@gymsystem.local', '+201111111111', 'admin', 1);

-- Benha Admin
-- Username: admin_benha, Password: admin123
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('admin_benha', '$2a$12$wlC0H3VYPdZ5e.oKpFvKQ.IJC7XmFf7ZzXLyuGBPQy1EBcBm5Jb3C',
 'Mohamed', 'Ali', 'admin.benha@gymsystem.local', '+201222222222', 'admin', 2);

-- COACH users (distributed across branches)
-- Cairo Coaches
-- Username: coach_cairo1, Password: coach123
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('coach_cairo1', '$2a$12$GqfFsHJZE3WK0CgU8JdOT.5TZRZJqYhQhM7rTx8xGxP0hG5Y1wY3W',
 'Karim', 'Mahmoud', 'karim.coach@gymsystem.local', '+201333333333', 'coach', 1);

-- Username: coach_cairo2, Password: coach123
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('coach_cairo2', '$2a$12$GqfFsHJZE3WK0CgU8JdOT.5TZRZJqYhQhM7rTx8xGxP0hG5Y1wY3W',
 'Sara', 'Ibrahim', 'sara.coach@gymsystem.local', '+201444444444', 'coach', 1);

-- Benha Coaches
-- Username: coach_benha1, Password: coach123
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('coach_benha1', '$2a$12$GqfFsHJZE3WK0CgU8JdOT.5TZRZJqYhQhM7rTx8xGxP0hG5Y1wY3W',
 'Omar', 'Khaled', 'omar.coach@gymsystem.local', '+201555555555', 'coach', 2);

-- Username: coach_benha2, Password: coach123
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, branch_id) VALUES
('coach_benha2', '$2a$12$GqfFsHJZE3WK0CgU8JdOT.5TZRZJqYhQhM7rTx8xGxP0hG5Y1wY3W',
 'Fatima', 'Ahmed', 'fatima.coach@gymsystem.local', '+201666666666', 'coach', 2);

-- ============================================================================
-- SEED DATA: Sample Members
-- ============================================================================
-- Cairo Branch Members (Branch ID: 1)
INSERT INTO members (random_id, first_name, last_name, mobile, email, height, weight, gender, 
                     date_of_birth, payment, start_date, end_date, period, assigned_branch, assigned_coach, notes) VALUES
(10234567, 'Hassan', 'Abdullah', '01012345678', 'hassan.abdullah@email.com', 175.5, 80.0, 'male',
 '1995-03-15', 500.00, '2025-01-01', '2025-01-31', '1 month', 1, 4, 'Beginner level, focus on cardio'),
 
(10234568, 'Mona', 'Said', '01112345679', 'mona.said@email.com', 165.0, 62.0, 'female',
 '1998-07-22', 1200.00, '2025-01-01', '2025-03-31', '3 months', 1, 5, 'Weight loss program'),
 
(10234569, 'Youssef', 'Gamal', '01212345680', 'youssef.gamal@email.com', 182.0, 90.0, 'male',
 '1992-11-08', 2000.00, '2025-01-01', '2025-06-30', '6 months', 1, 4, 'Advanced training, muscle building'),
 
(10234570, 'Heba', 'Mostafa', '01012345681', 'heba.mostafa@email.com', 168.0, 58.0, 'female',
 '2000-05-12', 800.00, '2025-01-15', '2025-03-15', '2 months', 1, 5, 'Yoga and flexibility focus'),
 
(10234571, 'Ahmed', 'Fathy', '01112345682', 'ahmed.fathy@email.com', 178.0, 85.0, 'male',
 '1996-09-30', 500.00, '2025-02-01', '2025-02-28', '1 month', 1, 4, 'Trial membership'),
 
(10234572, 'Nour', 'Salem', '01212345683', 'nour.salem@email.com', 170.0, 65.0, 'female',
 '1997-12-05', 1500.00, '2025-01-10', '2025-04-10', '3 months', 1, 5, 'Fitness and toning');

-- Benha Branch Members (Branch ID: 2)
INSERT INTO members (random_id, first_name, last_name, mobile, email, height, weight, gender,
                     date_of_birth, payment, start_date, end_date, period, assigned_branch, assigned_coach, notes) VALUES
(20234567, 'Mahmoud', 'Hussein', '01512345678', 'mahmoud.hussein@email.com', 180.0, 88.0, 'male',
 '1994-02-18', 600.00, '2025-01-05', '2025-02-04', '1 month', 2, 6, 'Strength training'),
 
(20234568, 'Laila', 'Kamal', '01512345679', 'laila.kamal@email.com', 162.0, 55.0, 'female',
 '1999-06-25', 1400.00, '2025-01-01', '2025-03-31', '3 months', 2, 7, 'Cardio and weight maintenance'),
 
(20234569, 'Ibrahim', 'Nabil', '01612345680', 'ibrahim.nabil@email.com', 176.0, 82.0, 'male',
 '1993-10-10', 2500.00, '2025-01-01', '2025-06-30', '6 months', 2, 6, 'CrossFit training'),
 
(20234570, 'Dina', 'Yousef', '01512345681', 'dina.yousef@email.com', 167.0, 60.0, 'female',
 '2001-04-20', 900.00, '2025-01-20', '2025-03-20', '2 months', 2, 7, 'Pilates and core strength'),
 
(20234571, 'Tarek', 'Adel', '01612345682', 'tarek.adel@email.com', 185.0, 95.0, 'male',
 '1991-08-15', 700.00, '2025-02-01', '2025-03-03', '1 month', 2, 6, 'Powerlifting preparation'),
 
(20234572, 'Yasmin', 'Hany', '01512345683', 'yasmin.hany@email.com', 163.0, 57.0, 'female',
 '1998-11-28', 1600.00, '2025-01-15', '2025-04-15', '3 months', 2, 7, 'Dance fitness program');

-- ============================================================================
-- SEED DATA: Training Progress Examples
-- ============================================================================
INSERT INTO training_progress (member_id, coach_id, session_date, notes, rating) VALUES
-- Cairo members progress
(1, 4, '2025-01-05', 'First session - baseline assessment. Good cardiovascular endurance. Recommended 3x weekly cardio sessions.', 4),
(1, 4, '2025-01-08', 'Treadmill 30min @ 6.5km/h, Cycling 15min. Heart rate stable. Increasing intensity next session.', 5),
(2, 5, '2025-01-03', 'Initial measurements taken. Created custom diet plan. Started with light cardio and stretching.', 4),
(3, 4, '2025-01-06', 'Advanced strength training: Bench press 5x5 @ 80kg, Squats 4x8 @ 100kg. Excellent form.', 5),

-- Benha members progress
(7, 6, '2025-01-07', 'Strength assessment completed. Deadlift 1RM: 120kg. Starting 5x5 program next week.', 4),
(8, 7, '2025-01-04', 'HIIT cardio session - 45min. Great endurance. Introduced interval training.', 5),
(9, 6, '2025-01-08', 'CrossFit WOD: 21-15-9 (Pull-ups, Push-ups, Squats). Time: 12:45. Personal best!', 5);

-- ============================================================================
-- SEED DATA: System Settings
-- ============================================================================
INSERT INTO system_settings (setting_key, setting_value, description, updated_by) VALUES
('system_name', 'Gym Management System', 'Application name displayed in UI', 1),
('smtp_enabled', 'false', 'Enable/disable email notifications (true/false)', 1),
('backup_enabled', 'true', 'Enable automatic database backups (true/false)', 1),
('session_timeout', '30', 'User session timeout in minutes', 1),
('password_min_length', '10', 'Minimum password length requirement', 1),
('membership_reminder_days', '7', 'Days before expiry to send reminder', 1);

-- ============================================================================
-- SEED DATA: Sample Audit Logs
-- ============================================================================
INSERT INTO audit_logs (user_id, action, target_id, target_type, details, ip_address) VALUES
(1, 'LOGIN_SUCCESS', 1, 'USER', 'Owner logged in successfully', '127.0.0.1'),
(2, 'LOGIN_SUCCESS', 2, 'USER', 'Admin Cairo logged in successfully', '127.0.0.1'),
(1, 'MEMBER_CREATE', 1, 'MEMBER', 'Created member: Hassan Abdullah', '127.0.0.1'),
(1, 'MEMBER_CREATE', 2, 'MEMBER', 'Created member: Mona Said', '127.0.0.1'),
(4, 'TRAINING_UPDATE', 1, 'MEMBER', 'Updated training progress for member Hassan Abdullah', '127.0.0.1');

-- ============================================================================
-- SEED DATA: Legacy regist table (for old LOGIN.java)
-- ============================================================================
-- Insert default test accounts for old login interface
-- NOTE: These are PLAIN TEXT passwords for testing only!
-- In production, use hashed passwords via BCrypt or PBKDF2
INSERT INTO regist (first_name, last_name, username, password, role, security_question, security_answer) VALUES
('System', 'Owner', 'owner', 'owner123', 'owner', 'What is your favorite color?', 'blue'),
('Admin', 'User', 'admin', 'admin123', 'admin', 'What is your pet name?', 'buddy'),
('Coach', 'User', 'coach', 'coach123', 'coach', 'What city were you born?', 'cairo');

-- ============================================================================
-- VIEWS: Useful database views for reporting
-- ============================================================================

-- View: Active members with coach information
CREATE VIEW view_active_members AS
SELECT 
    m.member_id,
    m.random_id,
    CONCAT(m.first_name, ' ', m.last_name) AS member_name,
    m.mobile,
    m.email,
    b.branch_name,
    CONCAT(u.first_name, ' ', u.last_name) AS coach_name,
    m.start_date,
    m.end_date,
    m.payment,
    DATEDIFF(m.end_date, CURDATE()) AS days_remaining,
    CASE 
        WHEN m.end_date < CURDATE() THEN 'EXPIRED'
        WHEN DATEDIFF(m.end_date, CURDATE()) <= 7 THEN 'EXPIRING_SOON'
        ELSE 'ACTIVE'
    END AS membership_status
FROM members m
JOIN branches b ON m.assigned_branch = b.branch_id
LEFT JOIN users u ON m.assigned_coach = u.user_id
WHERE m.is_active = TRUE
ORDER BY m.end_date ASC;

-- View: Revenue by branch
CREATE VIEW view_revenue_by_branch AS
SELECT 
    b.branch_id,
    b.branch_name,
    COUNT(m.member_id) AS total_members,
    COUNT(CASE WHEN m.end_date >= CURDATE() THEN 1 END) AS active_members,
    SUM(m.payment) AS total_revenue,
    AVG(m.payment) AS avg_payment
FROM branches b
LEFT JOIN members m ON b.branch_id = m.assigned_branch
GROUP BY b.branch_id, b.branch_name;

-- View: Coach workload
CREATE VIEW view_coach_workload AS
SELECT 
    u.user_id,
    CONCAT(u.first_name, ' ', u.last_name) AS coach_name,
    b.branch_name,
    COUNT(m.member_id) AS assigned_members,
    COUNT(CASE WHEN m.end_date >= CURDATE() THEN 1 END) AS active_assigned
FROM users u
JOIN branches b ON u.branch_id = b.branch_id
LEFT JOIN members m ON u.user_id = m.assigned_coach AND m.is_active = TRUE
WHERE u.role = 'coach' AND u.is_active = TRUE
GROUP BY u.user_id, coach_name, b.branch_name;

-- ============================================================================
-- STORED PROCEDURES
-- ============================================================================

-- Procedure: Get member statistics for a specific branch
DELIMITER //
CREATE PROCEDURE sp_get_branch_statistics(IN p_branch_id INT)
BEGIN
    SELECT 
        COUNT(*) AS total_members,
        COUNT(CASE WHEN end_date >= CURDATE() THEN 1 END) AS active_members,
        COUNT(CASE WHEN end_date < CURDATE() THEN 1 END) AS expired_members,
        SUM(payment) AS total_revenue,
        AVG(payment) AS average_payment,
        MIN(start_date) AS earliest_membership,
        MAX(end_date) AS latest_expiry
    FROM members
    WHERE assigned_branch = p_branch_id;
END //
DELIMITER ;

-- Procedure: Generate random 8-digit member ID
DELIMITER //
CREATE PROCEDURE sp_generate_random_id(OUT p_random_id INT)
BEGIN
    DECLARE v_exists INT DEFAULT 1;
    DECLARE v_random INT;
    
    WHILE v_exists > 0 DO
        -- Generate random 8-digit number (10000000 to 99999999)
        SET v_random = FLOOR(10000000 + (RAND() * 90000000));
        
        -- Check if it exists
        SELECT COUNT(*) INTO v_exists FROM members WHERE random_id = v_random;
    END WHILE;
    
    SET p_random_id = v_random;
END //
DELIMITER ;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Trigger: Auto-update member status based on end_date
DELIMITER //
CREATE TRIGGER trg_member_status_update
BEFORE UPDATE ON members
FOR EACH ROW
BEGIN
    IF NEW.end_date < CURDATE() THEN
        SET NEW.is_active = FALSE;
    END IF;
END //
DELIMITER ;

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
-- Additional composite indexes for common queries
CREATE INDEX idx_members_branch_active ON members(assigned_branch, is_active, end_date);
CREATE INDEX idx_members_coach_active ON members(assigned_coach, is_active, end_date);
CREATE INDEX idx_audit_user_time ON audit_logs(user_id, timestamp);

-- ============================================================================
-- DATABASE SETUP COMPLETE
-- ============================================================================

SELECT '========================================' AS '';
SELECT 'DATABASE SETUP COMPLETED SUCCESSFULLY!' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT 'Database: gymm' AS '';
SELECT 'Charset: utf8mb4 (supports Arabic & Emoji)' AS '';
SELECT '' AS '';
SELECT '--- DEFAULT LOGIN CREDENTIALS ---' AS '';
SELECT '' AS '';
SELECT 'NEW SYSTEM (LoginView.java):' AS '';
SELECT 'OWNER:' AS '';
SELECT '  Username: owner' AS '';
SELECT '  Password: owner123' AS '';
SELECT '  Email: m9nx11@gmail.com' AS '';
SELECT '' AS '';
SELECT 'ADMINS:' AS '';
SELECT '  Cairo  -> Username: admin_cairo  | Password: admin123' AS '';
SELECT '  Benha  -> Username: admin_benha  | Password: admin123' AS '';
SELECT '' AS '';
SELECT 'COACHES:' AS '';
SELECT '  Cairo  -> coach_cairo1, coach_cairo2 | Password: coach123' AS '';
SELECT '  Benha  -> coach_benha1 | Password: coach123' AS '';
SELECT '' AS '';
SELECT 'OLD LOGIN (LOGIN.java - Legacy):' AS '';
SELECT '  Username: owner   | Password: owner123' AS '';
SELECT '  Username: admin   | Password: admin123' AS '';
SELECT '  Username: coach   | Password: coach123' AS '';
SELECT '  Benha  -> coach_benha1, coach_benha2 | Password: coach123' AS '';
SELECT '' AS '';
SELECT '--- DATABASE STATISTICS ---' AS '';
SELECT CONCAT('Branches: ', COUNT(*)) AS '' FROM branches;
SELECT CONCAT('Users: ', COUNT(*)) AS '' FROM users;
SELECT CONCAT('Members: ', COUNT(*)) AS '' FROM members;
SELECT CONCAT('Training Records: ', COUNT(*)) AS '' FROM training_progress;
SELECT CONCAT('Audit Logs: ', COUNT(*)) AS '' FROM audit_logs;
SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'IMPORTANT SECURITY NOTES:' AS '';
SELECT '1. Change ALL passwords immediately!' AS '';
SELECT '2. Update SMTP settings in config' AS '';
SELECT '3. Use PasswordUtil.java to hash passwords' AS '';
SELECT '4. Never commit passwords to version control' AS '';
SELECT '========================================' AS '';
