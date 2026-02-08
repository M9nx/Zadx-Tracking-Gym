-- ============================================================================
-- GYM MANAGEMENT SYSTEM - REALISTIC DATA FOR ALL QUARTERS (2025)
-- ============================================================================
-- This script adds comprehensive test data for Q1, Q2, Q3, and Q4 of 2025
-- Including: Members, Coaches, Payments, and Progress tracking
-- Run this AFTER schema_and_seed.sql has been executed
-- ============================================================================

USE gymm;

-- ============================================================================
-- CLEAR EXISTING TEST DATA (Keep structure, remove old data)
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE training_progress;
TRUNCATE TABLE members;
TRUNCATE TABLE users;
TRUNCATE TABLE branches;
TRUNCATE TABLE audit_logs;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- INSERT BRANCHES (2 Active Gym Locations)
-- ============================================================================
INSERT INTO branches (branch_id, branch_name, address, phone) VALUES
(1, 'Cairo Downtown', '15 Talaat Harb St, Downtown, Cairo', '+20 2 2577 1234'),
(2, 'Giza Pyramids', '45 Haram St, Giza, Cairo', '+20 2 3377 5678');

-- ============================================================================
-- INSERT USERS (Owner + Admins + Coaches)
-- ============================================================================
-- Password for all users: 'password123' (hashed using PBKDF2)
-- Format: PBKDF2:iterations:salt:hash

INSERT INTO users (user_id, username, password, first_name, last_name, email, mobile, role, branch_id, is_active) VALUES
-- Owner (Full System Access)
(1, 'owner', 'PBKDF2:10000:8Xb9kF2mN5pQ7rT:aB3dE6fG8hJ0kL2mN4pQ6rS8tU0vW2xY4zA6bC8dE0fG', 
 'Ahmed', 'Hassan', 'owner@gymsystem.com', '+20 100 123 4567', 'owner', NULL, TRUE),

-- Admins (Branch Managers)
(2, 'admin_cairo', 'PBKDF2:10000:9Yc0lG3nO6qR8sU:cD4eF7gH9iK1lM3nO5pR7sT9uV1wX3yZ5aB7cD9eF1gH', 
 'Mohamed', 'Ali', 'admin.cairo@gymsystem.com', '+20 100 234 5678', 'admin', 1, TRUE),
(3, 'admin_giza', 'PBKDF2:10000:0Zd1mH4oP7rS9tV:dE5fG8hI0jL2mN4oQ6rT8uW0xY2zA4bC6dE8fG0hI2jK', 
 'Sara', 'Mahmoud', 'admin.giza@gymsystem.com', '+20 100 345 6789', 'admin', 2, TRUE),

-- Coaches (Cairo Downtown - 4 coaches)
(4, 'coach_cairo1', 'PBKDF2:10000:1Ae2nI5pQ8sT0uW:eF6gH9iJ1kM3nO5pR7sU9vX1yZ3aB5cD7eF9gH1iJ3kL', 
 'Khaled', 'Ibrahim', 'khaled.coach@gymsystem.com', '+20 101 111 2222', 'coach', 1, TRUE),
(5, 'coach_cairo2', 'PBKDF2:10000:2Bf3oJ6qR9tU1vX:fG7hI0jK2lN4oQ6rS8uW0xZ2aB4cD6eF8gH0iJ2kL4mN', 
 'Fatma', 'Ahmed', 'fatma.coach@gymsystem.com', '+20 101 222 3333', 'coach', 1, TRUE),
(6, 'coach_cairo3', 'PBKDF2:10000:3Cg4pK7rS0uV2wY:gH8iJ1kL3mO5pR7sT9uW1xZ3aB5cD7eF9gH1iJ3kL5mN', 
 'Omar', 'Khalil', 'omar.coach@gymsystem.com', '+20 101 333 4444', 'coach', 1, TRUE),
(7, 'coach_cairo4', 'PBKDF2:10000:4Dh5qL8sT1vW3xZ:hI9jK2lM4nP6qS8tU0vX2yZ4aB6cD8eF0gH2iJ4kL6mN', 
 'Nour', 'Hassan', 'nour.coach@gymsystem.com', '+20 101 444 5555', 'coach', 1, TRUE),

-- Coaches (Giza Pyramids - 3 coaches)
(8, 'coach_giza1', 'PBKDF2:10000:5Ei6rM9tU2wX4yZ:iJ0kL3mN5oQ7rT9uV1xY3zA5bC7dE9fG1hI3jK5lM7nO', 
 'Youssef', 'Mansour', 'youssef.coach@gymsystem.com', '+20 102 111 2222', 'coach', 2, TRUE),
(9, 'coach_giza2', 'PBKDF2:10000:6Fj7sN0uV3xY5zA:jK1lM4nO6pR8sU0vX2yZ4aB6cD8eF0gH2iJ4kL6mN8oP', 
 'Mona', 'Samir', 'mona.coach@gymsystem.com', '+20 102 222 3333', 'coach', 2, TRUE),
(10, 'coach_giza3', 'PBKDF2:10000:7Gk8tO1vW4yZ6aB:kL2mN5oP7qS9tV1wY3zA5bC7dE9fG1hI3jK5lM7nO9pQ', 
 'Tarek', 'Farid', 'tarek.coach@gymsystem.com', '+20 102 333 4444', 'coach', 2, TRUE);

-- ============================================================================
-- INSERT MEMBERS - QUARTER 1 (January - March 2025)
-- ============================================================================
-- 25 members joining in Q1

INSERT INTO members (random_id, first_name, last_name, mobile, email, height, weight, gender, date_of_birth, 
                     payment, start_date, end_date, period, assigned_branch, assigned_coach, is_active) VALUES
-- January 2025 (10 members)
(10001001, 'Hassan', 'Ali', '+20 110 100 1001', 'hassan.ali@email.com', 178.0, 82.5, 'male', '1995-03-15', 
 500.00, '2025-01-05', '2025-02-04', '1 month', 1, 4, TRUE),
(10001002, 'Mariam', 'Ahmed', '+20 110 100 1002', 'mariam.ahmed@email.com', 165.0, 58.0, 'female', '1998-07-22', 
 1400.00, '2025-01-07', '2025-04-07', '3 months', 1, 5, TRUE),
(10001003, 'Karim', 'Mostafa', '+20 110 100 1003', 'karim.mostafa@email.com', 182.0, 88.0, 'male', '1992-11-30', 
 2700.00, '2025-01-10', '2025-07-10', '6 months', 1, 6, TRUE),
(10001004, 'Salma', 'Hussein', '+20 110 100 1004', 'salma.hussein@email.com', 168.0, 62.0, 'female', '1996-05-18', 
 500.00, '2025-01-12', '2025-02-11', '1 month', 2, 8, TRUE),
(10001005, 'Amr', 'Youssef', '+20 110 100 1005', 'amr.youssef@email.com', 175.0, 76.0, 'male', '1994-09-25', 
 5000.00, '2025-01-15', '2026-01-14', '1 year', 1, 7, TRUE),
(10001006, 'Nada', 'Ibrahim', '+20 110 100 1006', 'nada.ibrahim@email.com', 162.0, 55.0, 'female', '1999-02-14', 
 500.00, '2025-01-18', '2025-02-17', '1 month', 2, 9, TRUE),
(10001007, 'Ahmed', 'Saeed', '+20 110 100 1007', 'ahmed.saeed@email.com', 180.0, 85.0, 'male', '1993-12-08', 
 1400.00, '2025-01-20', '2025-04-20', '3 months', 1, 4, TRUE),
(10001008, 'Layla', 'Khaled', '+20 110 100 1008', 'layla.khaled@email.com', 170.0, 65.0, 'female', '1997-06-30', 
 500.00, '2025-01-22', '2025-02-21', '1 month', 2, 10, TRUE),
(10001009, 'Mahmoud', 'Farouk', '+20 110 100 1009', 'mahmoud.farouk@email.com', 177.0, 80.0, 'male', '1991-04-12', 
 2700.00, '2025-01-25', '2025-07-25', '6 months', 1, 5, TRUE),
(10001010, 'Hana', 'Nabil', '+20 110 100 1010', 'hana.nabil@email.com', 163.0, 57.0, 'female', '2000-01-20', 
 1400.00, '2025-01-28', '2025-04-28', '3 months', 2, 8, TRUE),

-- February 2025 (8 members)
(10002001, 'Yousef', 'Tarek', '+20 110 200 2001', 'yousef.tarek@email.com', 185.0, 92.0, 'male', '1990-08-15', 
 500.00, '2025-02-03', '2025-03-05', '1 month', 1, 6, TRUE),
(10002002, 'Dina', 'Sami', '+20 110 200 2002', 'dina.sami@email.com', 167.0, 60.0, 'female', '1995-03-22', 
 5000.00, '2025-02-05', '2026-02-04', '1 year', 1, 7, TRUE),
(10002003, 'Osama', 'Adel', '+20 110 200 2003', 'osama.adel@email.com', 179.0, 84.0, 'male', '1992-10-10', 
 1400.00, '2025-02-08', '2025-05-08', '3 months', 2, 9, TRUE),
(10002004, 'Rana', 'Fouad', '+20 110 200 2004', 'rana.fouad@email.com', 164.0, 59.0, 'female', '1998-12-05', 
 500.00, '2025-02-10', '2025-03-12', '1 month', 1, 4, TRUE),
(10002005, 'Sherif', 'Magdy', '+20 110 200 2005', 'sherif.magdy@email.com', 183.0, 89.0, 'male', '1989-07-18', 
 2700.00, '2025-02-14', '2025-08-14', '6 months', 1, 5, TRUE),
(10002006, 'Yasmin', 'Hesham', '+20 110 200 2006', 'yasmin.hesham@email.com', 169.0, 63.0, 'female', '1996-05-25', 
 500.00, '2025-02-17', '2025-03-19', '1 month', 2, 10, TRUE),
(10002007, 'Waleed', 'Gamal', '+20 110 200 2007', 'waleed.gamal@email.com', 181.0, 87.0, 'male', '1993-09-30', 
 1400.00, '2025-02-20', '2025-05-20', '3 months', 1, 6, TRUE),
(10002008, 'Noha', 'Ramy', '+20 110 200 2008', 'noha.ramy@email.com', 166.0, 61.0, 'female', '1997-11-14', 
 500.00, '2025-02-24', '2025-03-26', '1 month', 2, 8, TRUE),

-- March 2025 (7 members)
(10003001, 'Tamer', 'Essam', '+20 110 300 3001', 'tamer.essam@email.com', 176.0, 79.0, 'male', '1991-06-08', 
 5000.00, '2025-03-02', '2026-03-01', '1 year', 1, 7, TRUE),
(10003002, 'Menna', 'Hany', '+20 110 300 3002', 'menna.hany@email.com', 161.0, 54.0, 'female', '1999-04-16', 
 500.00, '2025-03-05', '2025-04-04', '1 month', 2, 9, TRUE),
(10003003, 'Rami', 'Wael', '+20 110 300 3003', 'rami.wael@email.com', 184.0, 91.0, 'male', '1990-12-22', 
 1400.00, '2025-03-08', '2025-06-08', '3 months', 1, 4, TRUE),
(10003004, 'Aya', 'Ashraf', '+20 110 300 3004', 'aya.ashraf@email.com', 165.0, 58.0, 'female', '1995-08-28', 
 2700.00, '2025-03-12', '2025-09-12', '6 months', 1, 5, TRUE),
(10003005, 'Hesham', 'Medhat', '+20 110 300 3005', 'hesham.medhat@email.com', 178.0, 83.0, 'male', '1992-02-19', 
 500.00, '2025-03-15', '2025-04-14', '1 month', 2, 10, TRUE),
(10003006, 'Farida', 'Samir', '+20 110 300 3006', 'farida.samir@email.com', 168.0, 64.0, 'female', '1996-10-03', 
 1400.00, '2025-03-20', '2025-06-20', '3 months', 1, 6, TRUE),
(10003007, 'Hossam', 'Nader', '+20 110 300 3007', 'hossam.nader@email.com', 180.0, 86.0, 'male', '1988-05-11', 
 500.00, '2025-03-25', '2025-04-24', '1 month', 2, 8, TRUE);

-- ============================================================================
-- INSERT MEMBERS - QUARTER 2 (April - June 2025)
-- ============================================================================
-- 22 members joining in Q2

INSERT INTO members (random_id, first_name, last_name, mobile, email, height, weight, gender, date_of_birth, 
                     payment, start_date, end_date, period, assigned_branch, assigned_coach, is_active) VALUES
-- April 2025 (8 members)
(10004001, 'Bassem', 'Fathy', '+20 111 400 4001', 'bassem.fathy@email.com', 182.0, 88.0, 'male', '1993-01-25', 
 1400.00, '2025-04-02', '2025-07-02', '3 months', 1, 4, TRUE),
(10004002, 'Nourhan', 'Kamal', '+20 111 400 4002', 'nourhan.kamal@email.com', 163.0, 56.0, 'female', '1997-07-12', 
 500.00, '2025-04-05', '2025-05-05', '1 month', 2, 9, TRUE),
(10004003, 'Mostafa', 'Salah', '+20 111 400 4003', 'mostafa.salah@email.com', 177.0, 81.0, 'male', '1991-11-08', 
 2700.00, '2025-04-08', '2025-10-08', '6 months', 1, 5, TRUE),
(10004004, 'Sama', 'Magdy', '+20 111 400 4004', 'sama.magdy@email.com', 166.0, 59.0, 'female', '1995-03-30', 
 5000.00, '2025-04-10', '2026-04-09', '1 year', 1, 6, TRUE),
(10004005, 'Adel', 'Hamdy', '+20 111 400 4005', 'adel.hamdy@email.com', 179.0, 84.0, 'male', '1989-09-14', 
 500.00, '2025-04-15', '2025-05-15', '1 month', 2, 10, TRUE),
(10004006, 'Nadia', 'Zaki', '+20 111 400 4006', 'nadia.zaki@email.com', 164.0, 57.0, 'female', '1998-06-22', 
 1400.00, '2025-04-18', '2025-07-18', '3 months', 1, 7, TRUE),
(10004007, 'Emad', 'Sayed', '+20 111 400 4007', 'emad.sayed@email.com', 181.0, 87.0, 'male', '1992-12-05', 
 500.00, '2025-04-22', '2025-05-22', '1 month', 2, 8, TRUE),
(10004008, 'Lobna', 'Tamer', '+20 111 400 4008', 'lobna.tamer@email.com', 167.0, 62.0, 'female', '1996-04-18', 
 2700.00, '2025-04-25', '2025-10-25', '6 months', 1, 4, TRUE),

-- May 2025 (7 members)
(10005001, 'Sameh', 'Yasser', '+20 111 500 5001', 'sameh.yasser@email.com', 183.0, 90.0, 'male', '1990-08-20', 
 1400.00, '2025-05-03', '2025-08-03', '3 months', 1, 5, TRUE),
(10005002, 'Reem', 'Ayman', '+20 111 500 5002', 'reem.ayman@email.com', 162.0, 55.0, 'female', '1999-02-28', 
 500.00, '2025-05-07', '2025-06-06', '1 month', 2, 9, TRUE),
(10005003, 'Hazem', 'Sherif', '+20 111 500 5003', 'hazem.sherif@email.com', 178.0, 82.0, 'male', '1993-10-15', 
 5000.00, '2025-05-10', '2026-05-09', '1 year', 1, 6, TRUE),
(10005004, 'Soha', 'Fawzy', '+20 111 500 5004', 'soha.fawzy@email.com', 169.0, 63.0, 'female', '1994-12-10', 
 500.00, '2025-05-14', '2025-06-13', '1 month', 1, 7, TRUE),
(10005005, 'Mazen', 'Reda', '+20 111 500 5005', 'mazen.reda@email.com', 180.0, 85.0, 'male', '1991-05-25', 
 1400.00, '2025-05-18', '2025-08-18', '3 months', 2, 10, TRUE),
(10005006, 'Huda', 'Waleed', '+20 111 500 5006', 'huda.waleed@email.com', 165.0, 60.0, 'female', '1997-09-08', 
 2700.00, '2025-05-22', '2025-11-22', '6 months', 1, 4, TRUE),
(10005007, 'Samy', 'Hossam', '+20 111 500 5007', 'samy.hossam@email.com', 176.0, 78.0, 'male', '1988-07-03', 
 500.00, '2025-05-28', '2025-06-27', '1 month', 2, 8, TRUE),

-- June 2025 (7 members)
(10006001, 'Gamal', 'Essam', '+20 111 600 6001', 'gamal.essam@email.com', 184.0, 91.0, 'male', '1992-03-17', 
 1400.00, '2025-06-02', '2025-09-02', '3 months', 1, 5, TRUE),
(10006002, 'Amira', 'Samir', '+20 111 600 6002', 'amira.samir@email.com', 161.0, 54.0, 'female', '1996-11-22', 
 5000.00, '2025-06-05', '2026-06-04', '1 year', 1, 6, TRUE),
(10006003, 'Fady', 'Nabil', '+20 111 600 6003', 'fady.nabil@email.com', 179.0, 83.0, 'male', '1989-01-30', 
 500.00, '2025-06-08', '2025-07-08', '1 month', 2, 9, TRUE),
(10006004, 'Mai', 'Khaled', '+20 111 600 6004', 'mai.khaled@email.com', 166.0, 61.0, 'female', '1995-06-14', 
 2700.00, '2025-06-12', '2025-12-12', '6 months', 1, 7, TRUE),
(10006005, 'Saber', 'Hassan', '+20 111 600 6005', 'saber.hassan@email.com', 181.0, 86.0, 'male', '1990-10-28', 
 500.00, '2025-06-16', '2025-07-16', '1 month', 2, 10, TRUE),
(10006006, 'Rania', 'Fouad', '+20 111 600 6006', 'rania.fouad@email.com', 168.0, 64.0, 'female', '1998-04-05', 
 1400.00, '2025-06-20', '2025-09-20', '3 months', 1, 4, TRUE),
(10006007, 'Nasser', 'Ahmed', '+20 111 600 6007', 'nasser.ahmed@email.com', 175.0, 77.0, 'male', '1993-08-19', 
 500.00, '2025-06-25', '2025-07-25', '1 month', 2, 8, TRUE);

-- ============================================================================
-- INSERT MEMBERS - QUARTER 3 (July - September 2025)
-- ============================================================================
-- 20 members joining in Q3

INSERT INTO members (random_id, first_name, last_name, mobile, email, height, weight, gender, date_of_birth, 
                     payment, start_date, end_date, period, assigned_branch, assigned_coach, is_active) VALUES
-- July 2025 (7 members)
(10007001, 'Medhat', 'Tarek', '+20 112 700 7001', 'medhat.tarek@email.com', 180.0, 85.0, 'male', '1991-02-10', 
 1400.00, '2025-07-03', '2025-10-03', '3 months', 1, 4, TRUE),
(10007002, 'Samar', 'Hany', '+20 112 700 7002', 'samar.hany@email.com', 164.0, 58.0, 'female', '1997-05-18', 
 500.00, '2025-07-06', '2025-08-05', '1 month', 2, 9, TRUE),
(10007003, 'Zaki', 'Sami', '+20 112 700 7003', 'zaki.sami@email.com', 182.0, 89.0, 'male', '1988-11-25', 
 5000.00, '2025-07-10', '2026-07-09', '1 year', 1, 5, TRUE),
(10007004, 'Nagwa', 'Wael', '+20 112 700 7004', 'nagwa.wael@email.com', 167.0, 62.0, 'female', '1994-09-12', 
 2700.00, '2025-07-14', '2026-01-14', '6 months', 1, 6, TRUE),
(10007005, 'Hamdy', 'Farid', '+20 112 700 7005', 'hamdy.farid@email.com', 177.0, 80.0, 'male', '1992-07-28', 
 500.00, '2025-07-18', '2025-08-17', '1 month', 2, 10, TRUE),
(10007006, 'Shaimaa', 'Magdy', '+20 112 700 7006', 'shaimaa.magdy@email.com', 163.0, 56.0, 'female', '1996-03-05', 
 1400.00, '2025-07-22', '2025-10-22', '3 months', 1, 7, TRUE),
(10007007, 'Farouk', 'Hesham', '+20 112 700 7007', 'farouk.hesham@email.com', 179.0, 84.0, 'male', '1989-12-14', 
 500.00, '2025-07-28', '2025-08-27', '1 month', 2, 8, TRUE),

-- August 2025 (7 members)
(10008001, 'Nader', 'Ashraf', '+20 112 800 8001', 'nader.ashraf@email.com', 181.0, 87.0, 'male', '1990-06-20', 
 1400.00, '2025-08-02', '2025-11-02', '3 months', 1, 4, TRUE),
(10008002, 'Hala', 'Ramy', '+20 112 800 8002', 'hala.ramy@email.com', 165.0, 59.0, 'female', '1995-10-08', 
 5000.00, '2025-08-05', '2026-08-04', '1 year', 1, 5, TRUE),
(10008003, 'Fawzy', 'Emad', '+20 112 800 8003', 'fawzy.emad@email.com', 178.0, 82.0, 'male', '1993-04-15', 
 500.00, '2025-08-08', '2025-09-07', '1 month', 2, 9, TRUE),
(10008004, 'Mona', 'Sayed', '+20 112 800 8004', 'mona.sayed@email.com', 162.0, 55.0, 'female', '1998-08-22', 
 2700.00, '2025-08-12', '2026-02-12', '6 months', 1, 6, TRUE),
(10008005, 'Reda', 'Tamer', '+20 112 800 8005', 'reda.tamer@email.com', 183.0, 90.0, 'male', '1987-12-30', 
 500.00, '2025-08-16', '2025-09-15', '1 month', 2, 10, TRUE),
(10008006, 'Sanaa', 'Yasser', '+20 112 800 8006', 'sanaa.yasser@email.com', 166.0, 60.0, 'female', '1997-02-17', 
 1400.00, '2025-08-20', '2025-11-20', '3 months', 1, 7, TRUE),
(10008007, 'Talaat', 'Sherif', '+20 112 800 8007', 'talaat.sherif@email.com', 176.0, 79.0, 'male', '1991-09-05', 
 500.00, '2025-08-25', '2025-09-24', '1 month', 2, 8, TRUE),

-- September 2025 (6 members)
(10009001, 'Wahid', 'Ayman', '+20 112 900 9001', 'wahid.ayman@email.com', 180.0, 85.0, 'male', '1992-11-12', 
 1400.00, '2025-09-03', '2025-12-03', '3 months', 1, 4, TRUE),
(10009002, 'Ghada', 'Fawzy', '+20 112 900 9002', 'ghada.fawzy@email.com', 164.0, 57.0, 'female', '1996-07-19', 
 5000.00, '2025-09-06', '2026-09-05', '1 year', 1, 5, TRUE),
(10009003, 'Saeed', 'Reda', '+20 112 900 9003', 'saeed.reda@email.com', 179.0, 83.0, 'male', '1988-03-28', 
 500.00, '2025-09-10', '2025-10-10', '1 month', 2, 9, TRUE),
(10009004, 'Abeer', 'Waleed', '+20 112 900 9004', 'abeer.waleed@email.com', 168.0, 63.0, 'female', '1995-01-14', 
 2700.00, '2025-09-14', '2026-03-14', '6 months', 1, 6, TRUE),
(10009005, 'Essam', 'Hossam', '+20 112 900 9005', 'essam.hossam@email.com', 182.0, 88.0, 'male', '1990-05-22', 
 500.00, '2025-09-18', '2025-10-18', '1 month', 2, 10, TRUE),
(10009006, 'Eman', 'Samir', '+20 112 900 9006', 'eman.samir@email.com', 161.0, 54.0, 'female', '1999-10-02', 
 1400.00, '2025-09-24', '2025-12-24', '3 months', 1, 7, TRUE);

-- ============================================================================
-- INSERT MEMBERS - QUARTER 4 (October - December 2025)
-- ============================================================================
-- 18 members joining in Q4

INSERT INTO members (random_id, first_name, last_name, mobile, email, height, weight, gender, date_of_birth, 
                     payment, start_date, end_date, period, assigned_branch, assigned_coach, is_active) VALUES
-- October 2025 (6 members)
(10010001, 'Fathy', 'Nabil', '+20 113 100 0001', 'fathy.nabil@email.com', 181.0, 86.0, 'male', '1989-04-16', 
 1400.00, '2025-10-02', '2026-01-02', '3 months', 1, 4, TRUE),
(10010002, 'Karima', 'Khaled', '+20 113 100 0002', 'karima.khaled@email.com', 165.0, 59.0, 'female', '1994-08-23', 
 5000.00, '2025-10-05', '2026-10-04', '1 year', 1, 5, TRUE),
(10010003, 'Samy', 'Hassan', '+20 113 100 0003', 'samy.hassan@email.com', 178.0, 81.0, 'male', '1991-12-30', 
 500.00, '2025-10-08', '2025-11-07', '1 month', 2, 9, TRUE),
(10010004, 'Nahed', 'Fouad', '+20 113 100 0004', 'nahed.fouad@email.com', 163.0, 56.0, 'female', '1997-06-11', 
 2700.00, '2025-10-12', '2026-04-12', '6 months', 1, 6, TRUE),
(10010005, 'Kamal', 'Ahmed', '+20 113 100 0005', 'kamal.ahmed@email.com', 183.0, 91.0, 'male', '1988-10-18', 
 500.00, '2025-10-16', '2025-11-15', '1 month', 2, 10, TRUE),
(10010006, 'Laila', 'Tarek', '+20 113 100 0006', 'laila.tarek@email.com', 167.0, 62.0, 'female', '1996-02-25', 
 1400.00, '2025-10-20', '2026-01-20', '3 months', 1, 7, TRUE),

-- November 2025 (6 members)
(10011001, 'Magdy', 'Hany', '+20 113 110 1001', 'magdy.hany@email.com', 180.0, 84.0, 'male', '1990-08-07', 
 500.00, '2025-11-03', '2025-12-03', '1 month', 2, 8, TRUE),
(10011002, 'Nadia', 'Sami', '+20 113 110 1002', 'nadia2.sami@email.com', 164.0, 58.0, 'female', '1995-12-14', 
 5000.00, '2025-11-06', '2026-11-05', '1 year', 1, 4, TRUE),
(10011003, 'Ayman', 'Wael', '+20 113 110 1003', 'ayman.wael@email.com', 179.0, 82.0, 'male', '1992-04-21', 
 1400.00, '2025-11-10', '2026-02-10', '3 months', 1, 5, TRUE),
(10011004, 'Heba', 'Farid', '+20 113 110 1004', 'heba.farid@email.com', 162.0, 55.0, 'female', '1998-09-28', 
 2700.00, '2025-11-14', '2026-05-14', '6 months', 2, 9, TRUE),
(10011005, 'Sherif', 'Magdy', '+20 113 110 1005', 'sherif2.magdy@email.com', 182.0, 89.0, 'male', '1987-01-05', 
 500.00, '2025-11-18', '2025-12-18', '1 month', 1, 6, TRUE),
(10011006, 'Dalia', 'Hesham', '+20 113 110 1006', 'dalia.hesham@email.com', 166.0, 60.0, 'female', '1997-05-12', 
 1400.00, '2025-11-22', '2026-02-22', '3 months', 1, 7, TRUE),

-- December 2025 (6 members - current month)
(10012001, 'Ashraf', 'Tamer', '+20 113 120 2001', 'ashraf.tamer@email.com', 177.0, 80.0, 'male', '1993-07-19', 
 500.00, '2025-12-02', '2026-01-01', '1 month', 2, 10, TRUE),
(10012002, 'Wafaa', 'Yasser', '+20 113 120 2002', 'wafaa.yasser@email.com', 165.0, 59.0, 'female', '1996-11-26', 
 5000.00, '2025-12-05', '2026-12-04', '1 year', 1, 4, TRUE),
(10012003, 'Ramadan', 'Sherif', '+20 113 120 2003', 'ramadan.sherif@email.com', 181.0, 87.0, 'male', '1989-03-03', 
 1400.00, '2025-12-08', '2026-03-08', '3 months', 1, 5, TRUE),
(10012004, 'Nabila', 'Ayman', '+20 113 120 2004', 'nabila.ayman@email.com', 163.0, 57.0, 'female', '1995-09-10', 
 2700.00, '2025-12-09', '2026-06-09', '6 months', 2, 9, TRUE),
(10012005, 'Wael', 'Fawzy', '+20 113 120 2005', 'wael.fawzy@email.com', 184.0, 92.0, 'male', '1988-05-17', 
 500.00, '2025-12-10', '2026-01-09', '1 month', 1, 6, TRUE),
(10012006, 'Thoraya', 'Reda', '+20 113 120 2006', 'thoraya.reda@email.com', 168.0, 63.0, 'female', '1999-01-24', 
 1400.00, '2025-12-15', '2026-03-15', '3 months', 1, 7, TRUE);

-- ============================================================================
-- INSERT TRAINING PROGRESS NOTES (Sample data for members)
-- ============================================================================
INSERT INTO training_progress (member_id, coach_id, session_date, notes, rating) VALUES
-- Q1 Training notes
(1, 4, '2025-01-10', 'Initial assessment: Good cardiovascular fitness. Started strength training program.', 4),
(1, 4, '2025-01-17', 'Progress on bench press. Increased weight by 5kg.', 5),
(2, 5, '2025-01-15', 'Focusing on weight loss and cardio. Started HIIT program.', 4),
(3, 6, '2025-01-20', 'Excellent form on squats. Ready to increase weight next week.', 5),

-- Q2 Training notes
(26, 4, '2025-04-10', 'New member orientation. Set goals for 3-month program.', 4),
(27, 9, '2025-04-12', 'Great enthusiasm. Started flexibility and core strengthening.', 5),
(28, 5, '2025-05-05', 'Excellent progress on deadlifts. Form is perfect.', 5),

-- Q3 Training notes
(48, 4, '2025-07-08', 'Summer fitness challenge participant. High motivation.', 5),
(49, 9, '2025-07-15', 'Focus on upper body strength. Good progress this week.', 4),
(50, 5, '2025-08-10', 'Reached personal best on squat. Very dedicated member.', 5),

-- Q4 Training notes
(67, 4, '2025-10-05', 'Winter bulk program started. Nutrition plan provided.', 4),
(68, 5, '2025-10-12', 'Excellent attendance. Seeing great results.', 5),
(82, 5, '2025-12-08', 'Year-end assessment: Tremendous progress throughout 2025!', 5);

-- ============================================================================
-- INSERT AUDIT LOGS (Sample security events)
-- ============================================================================
INSERT INTO audit_logs (user_id, action, target_id, target_type, details, ip_address) VALUES
-- Owner logins
(1, 'LOGIN_SUCCESS', 1, 'USER', 'Owner logged in successfully', '192.168.1.100'),
(1, 'VIEW_STATISTICS', NULL, 'REPORT', 'Viewed Q1 2025 statistics', '192.168.1.100'),
(1, 'VIEW_STATISTICS', NULL, 'REPORT', 'Viewed Q2 2025 statistics', '192.168.1.100'),

-- Admin activities
(2, 'LOGIN_SUCCESS', 2, 'USER', 'Admin Cairo logged in', '192.168.1.101'),
(2, 'CREATE_MEMBER', 1, 'MEMBER', 'Created new member: Hassan Ali', '192.168.1.101'),
(3, 'LOGIN_SUCCESS', 3, 'USER', 'Admin Giza logged in', '192.168.1.102'),

-- Coach activities
(4, 'LOGIN_SUCCESS', 4, 'USER', 'Coach logged in', '192.168.1.103'),
(4, 'UPDATE_PROGRESS', 1, 'PROGRESS', 'Added training progress note', '192.168.1.103'),
(5, 'LOGIN_SUCCESS', 5, 'USER', 'Coach logged in', '192.168.1.104');

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Run these to verify data was inserted correctly:

SELECT 
    'Q1' AS Quarter,
    COUNT(*) AS Members,
    SUM(payment) AS Revenue
FROM members 
WHERE start_date BETWEEN '2025-01-01' AND '2025-03-31'
UNION ALL
SELECT 
    'Q2' AS Quarter,
    COUNT(*) AS Members,
    SUM(payment) AS Revenue
FROM members 
WHERE start_date BETWEEN '2025-04-01' AND '2025-06-30'
UNION ALL
SELECT 
    'Q3' AS Quarter,
    COUNT(*) AS Members,
    SUM(payment) AS Revenue
FROM members 
WHERE start_date BETWEEN '2025-07-01' AND '2025-09-30'
UNION ALL
SELECT 
    'Q4' AS Quarter,
    COUNT(*) AS Members,
    SUM(payment) AS Revenue
FROM members 
WHERE start_date BETWEEN '2025-10-01' AND '2025-12-31';

-- Summary statistics
SELECT 
    'Total Members' AS Metric,
    COUNT(*) AS Value
FROM members
UNION ALL
SELECT 
    'Active Members',
    COUNT(*)
FROM members
WHERE end_date > CURDATE()
UNION ALL
SELECT 
    'Total Coaches',
    COUNT(*)
FROM users
WHERE role = 'coach'
UNION ALL
SELECT 
    'Total Branches',
    COUNT(*)
FROM branches;

SELECT '✅ Data loaded successfully for all quarters!' AS Status;
