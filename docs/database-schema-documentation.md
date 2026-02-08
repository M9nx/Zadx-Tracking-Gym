# Database Schema Documentation

**Project:** Gym Management System  
**Database Name:** `gymm`  
**Database Engine:** MySQL 8.0+  
**Character Set:** utf8mb4 (Unicode support for Arabic and Emoji)  
**Collation:** utf8mb4_unicode_ci  
**Documentation Date:** December 16, 2025

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Entity Relationship Summary](#entity-relationship-summary)
3. [Table Definitions](#table-definitions)
   - [branches](#1-branches)
   - [users](#2-users)
   - [members](#3-members)
   - [training_progress](#4-training_progress)
   - [audit_logs](#5-audit_logs)
   - [system_settings](#6-system_settings)
   - [regist](#7-regist-legacy)
   - [add_member](#8-add_member-legacy)
4. [Database Views](#database-views)
5. [Stored Procedures](#stored-procedures)
6. [Triggers](#triggers)
7. [Schema Naming Conventions](#schema-naming-conventions)
8. [Data Integrity Rules](#data-integrity-rules)

---

## Database Overview

The Gym Management System database is designed to manage multi-branch gym operations, including:

- **Branch Management**: Multiple gym locations with independent operations
- **User Management**: Role-based access control (Owner, Admin, Coach)
- **Member Management**: Gym member profiles, subscriptions, and tracking
- **Training Tracking**: Coach-member training sessions and progress notes
- **Audit Logging**: Security and compliance tracking for all system actions
- **System Configuration**: Centralized application settings storage

**Purpose**: This database supports a comprehensive gym management system with role-based access control, multi-branch support, and complete audit trails for compliance and security.

---

## Entity Relationship Summary

### Primary Relationships

```
branches (1) ─────< users (M)
    │                  │
    │                  │
    │                  └─────< training_progress (M)
    │                           │
    │                           │
    └──────────────< members (M)
                         │
                         └────< training_progress (M)

users (1) ────< audit_logs (M)
users (1) ────< system_settings (M)
```

### Relationship Details

| From Table | To Table | Relationship Type | Foreign Key | Description |
|------------|----------|-------------------|-------------|-------------|
| branches | users | One-to-Many | users.branch_id | Each branch has multiple users |
| branches | members | One-to-Many | members.assigned_branch | Each branch has multiple members |
| users | members | One-to-Many | members.assigned_coach | Each coach manages multiple members |
| users | audit_logs | One-to-Many | audit_logs.user_id | Each user generates multiple audit logs |
| users | system_settings | One-to-Many | system_settings.updated_by | Each user can update settings |
| members | training_progress | One-to-Many | training_progress.member_id | Each member has multiple training sessions |
| users (coach) | training_progress | One-to-Many | training_progress.coach_id | Each coach records multiple sessions |

---

## Table Definitions

### 1. branches

**Purpose**: Stores gym branch locations and contact information.

**Module**: Branch Management

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| branch_id | INT | No | Yes | No | Yes | Unique branch identifier |
| branch_name | VARCHAR(100) | No | No | No | No | Branch name (unique) |
| address | VARCHAR(255) | Yes | No | No | No | Physical address of the branch |
| phone | VARCHAR(20) | Yes | No | No | No | Contact phone number |
| created_at | TIMESTAMP | No | No | No | No | Record creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | Last update timestamp |

#### Keys & Constraints

- **Primary Key**: `branch_id`
- **Unique Constraint**: `branch_name` (prevents duplicate branch names)
- **Default Values**:
  - `created_at`: CURRENT_TIMESTAMP
  - `updated_at`: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | branch_id | Primary key index |
| UNIQUE | branch_name | Ensure unique branch names |
| idx_branch_name | branch_name | Fast branch lookup by name |

#### Relationships

- **One-to-Many** with `users`: One branch can have multiple users (admins, coaches)
- **One-to-Many** with `members`: One branch can have multiple members

#### Table Dependencies

- **Depends On**: None
- **Depended By**: `users`, `members`

---

### 2. users

**Purpose**: Stores all system users with role-based access control (Owner, Admin, Coach).

**Module**: User Management & Authentication

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| user_id | INT | No | Yes | No | Yes | Unique user identifier |
| username | VARCHAR(50) | No | No | No | No | Login username (unique) |
| password | VARCHAR(255) | No | No | No | No | Hashed password (BCrypt/PBKDF2) |
| first_name | VARCHAR(100) | No | No | No | No | User's first name |
| last_name | VARCHAR(100) | No | No | No | No | User's last name |
| email | VARCHAR(100) | No | No | No | No | Email address (unique) |
| mobile | VARCHAR(20) | Yes | No | No | No | Mobile phone number |
| role | ENUM | No | No | No | No | User role: 'owner', 'admin', 'coach' |
| branch_id | INT | Yes | No | Yes | No | Assigned branch (NULL for owner) |
| is_active | BOOLEAN | No | No | No | No | Account active status |
| last_login | TIMESTAMP | Yes | No | No | No | Last successful login timestamp |
| created_at | TIMESTAMP | No | No | No | No | Account creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | Last update timestamp |

#### Keys & Constraints

- **Primary Key**: `user_id`
- **Foreign Keys**:
  - `branch_id` REFERENCES `branches(branch_id)` ON DELETE SET NULL
- **Unique Constraints**:
  - `username` (prevents duplicate usernames)
  - `email` (prevents duplicate emails)
- **Default Values**:
  - `is_active`: TRUE
  - `created_at`: CURRENT_TIMESTAMP
  - `updated_at`: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | user_id | Primary key index |
| UNIQUE | username | Fast unique username lookup |
| UNIQUE | email | Fast unique email lookup |
| idx_username | username | Authentication queries |
| idx_role | role | Role-based filtering |
| idx_branch_role | branch_id, role | Branch-specific user queries |

#### Relationships

- **Many-to-One** with `branches`: Each user belongs to one branch (NULL for owner)
- **One-to-Many** with `members`: Each coach manages multiple members
- **One-to-Many** with `training_progress`: Each coach records multiple training sessions
- **One-to-Many** with `audit_logs`: Each user generates audit logs
- **One-to-Many** with `system_settings`: Each user can update settings

#### Data Integrity Notes

- **Referential Integrity**: ON DELETE SET NULL for branch_id (user remains if branch deleted)
- **Password Storage**: Passwords MUST be hashed using BCrypt (60 chars) or PBKDF2
- **Role Hierarchy**: owner > admin > coach
- **Branch Assignment**: Owner has NULL branch_id (system-wide access)

#### Table Dependencies

- **Depends On**: `branches`
- **Depended By**: `members`, `training_progress`, `audit_logs`, `system_settings`

---

### 3. members

**Purpose**: Stores gym member information, subscriptions, and membership details.

**Module**: Member Management

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| member_id | INT | No | Yes | No | Yes | Unique member identifier |
| random_id | INT | No | No | No | No | Random 8-digit member card ID (unique) |
| first_name | VARCHAR(100) | No | No | No | No | Member's first name |
| last_name | VARCHAR(100) | No | No | No | No | Member's last name |
| mobile | VARCHAR(20) | No | No | No | No | Mobile phone number |
| email | VARCHAR(100) | Yes | No | No | No | Email address |
| height | DECIMAL(5,2) | Yes | No | No | No | Height in centimeters |
| weight | DECIMAL(5,2) | Yes | No | No | No | Weight in kilograms |
| gender | ENUM | No | No | No | No | Gender: 'male', 'female', 'other' |
| date_of_birth | DATE | Yes | No | No | No | Date of birth |
| payment | DECIMAL(10,2) | No | No | No | No | Payment amount for subscription |
| start_date | DATE | No | No | No | No | Membership start date |
| end_date | DATE | No | No | No | No | Membership expiration date |
| period | VARCHAR(50) | No | No | No | No | Subscription period description |
| assigned_branch | INT | No | No | Yes | No | Branch where member is registered |
| assigned_coach | INT | Yes | No | Yes | No | Coach responsible for this member |
| is_active | BOOLEAN | No | No | No | No | Membership active status |
| notes | TEXT | Yes | No | No | No | Additional notes about the member |
| created_at | TIMESTAMP | No | No | No | No | Record creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | Last update timestamp |

#### Keys & Constraints

- **Primary Key**: `member_id`
- **Foreign Keys**:
  - `assigned_branch` REFERENCES `branches(branch_id)` ON DELETE RESTRICT
  - `assigned_coach` REFERENCES `users(user_id)` ON DELETE SET NULL
- **Unique Constraint**: `random_id` (8-digit member card number)
- **Default Values**:
  - `is_active`: TRUE
  - `created_at`: CURRENT_TIMESTAMP
  - `updated_at`: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | member_id | Primary key index |
| UNIQUE | random_id | Fast member card lookup |
| idx_mobile | mobile | Search members by phone |
| idx_random_id | random_id | Member card validation |
| idx_branch | assigned_branch | Branch-specific queries |
| idx_coach | assigned_coach | Coach workload queries |
| idx_active | is_active | Filter active members |
| idx_dates | start_date, end_date | Expiration tracking |
| idx_members_branch_active | assigned_branch, is_active, end_date | Composite query optimization |
| idx_members_coach_active | assigned_coach, is_active, end_date | Coach-specific member lists |

#### Relationships

- **Many-to-One** with `branches`: Each member belongs to one branch
- **Many-to-One** with `users`: Each member is assigned to one coach
- **One-to-Many** with `training_progress`: Each member has multiple training sessions

#### Data Integrity Notes

- **Referential Integrity**:
  - ON DELETE RESTRICT for assigned_branch (cannot delete branch with members)
  - ON DELETE SET NULL for assigned_coach (member remains if coach deleted)
- **Member Card ID**: 8-digit random number generated via stored procedure
- **Status Update**: Trigger automatically sets is_active to FALSE when end_date passes

#### Table Dependencies

- **Depends On**: `branches`, `users`
- **Depended By**: `training_progress`

---

### 4. training_progress

**Purpose**: Tracks training sessions, progress notes, and ratings for members by coaches.

**Module**: Training & Progress Tracking

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| progress_id | INT | No | Yes | No | Yes | Unique progress record identifier |
| member_id | INT | No | No | Yes | No | Member receiving training |
| coach_id | INT | No | No | Yes | No | Coach conducting the session |
| session_date | DATE | No | No | No | No | Date of training session |
| notes | TEXT | No | No | No | No | Training notes, exercises, progress details |
| rating | INT | Yes | No | No | No | Session rating (1-5 scale) |
| created_at | TIMESTAMP | No | No | No | No | Record creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | Last update timestamp |

#### Keys & Constraints

- **Primary Key**: `progress_id`
- **Foreign Keys**:
  - `member_id` REFERENCES `members(member_id)` ON DELETE CASCADE
  - `coach_id` REFERENCES `users(user_id)` ON DELETE CASCADE
- **Check Constraint**: `rating BETWEEN 1 AND 5`
- **Default Values**:
  - `created_at`: CURRENT_TIMESTAMP
  - `updated_at`: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | progress_id | Primary key index |
| idx_member | member_id | Member training history |
| idx_coach | coach_id | Coach session records |
| idx_session_date | session_date | Date-based queries |

#### Relationships

- **Many-to-One** with `members`: Each training session belongs to one member
- **Many-to-One** with `users`: Each training session is conducted by one coach

#### Data Integrity Notes

- **Referential Integrity**: ON DELETE CASCADE (delete progress when member/coach deleted)
- **Rating Validation**: CHECK constraint ensures rating is between 1 and 5
- **Session Tracking**: Notes field stores detailed session information

#### Table Dependencies

- **Depends On**: `members`, `users`
- **Depended By**: None

---

### 5. audit_logs

**Purpose**: Tracks all sensitive system actions for security, compliance, and auditing.

**Module**: Security & Audit Management

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| audit_id | BIGINT | No | Yes | No | Yes | Unique audit log identifier |
| user_id | INT | Yes | No | Yes | No | User who performed the action (NULL for system) |
| action | VARCHAR(100) | No | No | No | No | Action type (LOGIN_SUCCESS, PASSWORD_RESET, etc.) |
| target_id | INT | Yes | No | No | No | ID of affected entity |
| target_type | VARCHAR(50) | Yes | No | No | No | Type of target (USER, MEMBER, BRANCH, etc.) |
| details | TEXT | Yes | No | No | No | Additional details (JSON or plain text) |
| ip_address | VARCHAR(45) | Yes | No | No | No | IP address of user (IPv4/IPv6) |
| timestamp | TIMESTAMP | No | No | No | No | When the action occurred |

#### Keys & Constraints

- **Primary Key**: `audit_id`
- **Foreign Keys**:
  - `user_id` REFERENCES `users(user_id)` ON DELETE SET NULL
- **Default Values**:
  - `timestamp`: CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | audit_id | Primary key index |
| idx_user_action | user_id, action | User activity queries |
| idx_timestamp | timestamp | Chronological queries |
| idx_action | action | Action-type filtering |
| idx_audit_user_time | user_id, timestamp | User timeline queries |

#### Relationships

- **Many-to-One** with `users`: Each audit log is associated with one user

#### Data Integrity Notes

- **Referential Integrity**: ON DELETE SET NULL (preserve audit log if user deleted)
- **System Events**: user_id is NULL for automated system actions
- **IP Storage**: VARCHAR(45) supports both IPv4 (15 chars) and IPv6 (45 chars)

#### Common Action Types

- `LOGIN_SUCCESS` - Successful user login
- `LOGIN_FAIL` - Failed login attempt
- `PASSWORD_RESET` - Password changed
- `MEMBER_CREATE` - New member added
- `MEMBER_UPDATE` - Member information updated
- `MEMBER_DELETE` - Member removed
- `USER_CREATE` - New user account created
- `TRAINING_UPDATE` - Training progress recorded

#### Table Dependencies

- **Depends On**: `users`
- **Depended By**: None

---

### 6. system_settings

**Purpose**: Stores system-wide configuration settings in key-value format.

**Module**: System Configuration

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| setting_id | INT | No | Yes | No | Yes | Unique setting identifier |
| setting_key | VARCHAR(100) | No | No | No | No | Setting key name (unique) |
| setting_value | TEXT | No | No | No | No | Setting value |
| description | VARCHAR(255) | Yes | No | No | No | Human-readable description |
| updated_by | INT | Yes | No | Yes | No | User who last updated this setting |
| updated_at | TIMESTAMP | No | No | No | No | Last update timestamp |

#### Keys & Constraints

- **Primary Key**: `setting_id`
- **Foreign Keys**:
  - `updated_by` REFERENCES `users(user_id)` ON DELETE SET NULL
- **Unique Constraint**: `setting_key` (prevents duplicate keys)
- **Default Values**:
  - `updated_at`: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | setting_id | Primary key index |
| UNIQUE | setting_key | Fast setting lookup by key |
| idx_key | setting_key | Setting retrieval queries |

#### Relationships

- **Many-to-One** with `users`: Each setting update is tracked to a user

#### Data Integrity Notes

- **Referential Integrity**: ON DELETE SET NULL (preserve setting if user deleted)
- **Key-Value Storage**: Flexible configuration storage pattern

#### Common Settings

| Setting Key | Example Value | Description |
|-------------|---------------|-------------|
| system_name | Gym Management System | Application display name |
| smtp_enabled | true/false | Email notifications toggle |
| email.sender_address | gym.zadx@gmail.com | Email sender address |
| email.smtp_host | smtp.gmail.com | SMTP server hostname |
| email.smtp_port | 465 | SMTP server port |
| email.smtp_username | gym.zadx@gmail.com | SMTP authentication username |
| email.smtp_password | [app password] | SMTP authentication password |
| email.smtp_tls | true/false | TLS/SSL encryption toggle |
| backup_enabled | true/false | Automatic backup toggle |
| session_timeout | 30 | Session timeout in minutes |
| password_min_length | 10 | Minimum password length |
| membership_reminder_days | 7 | Days before expiry to remind |

#### Table Dependencies

- **Depends On**: `users`
- **Depended By**: None

---

### 7. regist (LEGACY)

**Purpose**: Legacy user table for backward compatibility with old LOGIN.java interface.

**Module**: Legacy Authentication System

**Status**: DEPRECATED - Use `users` table for new development

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| id | INT | No | Yes | No | Yes | Unique user identifier |
| first_name | VARCHAR(100) | No | No | No | No | User's first name |
| last_name | VARCHAR(100) | No | No | No | No | User's last name |
| email | VARCHAR(255) | Yes | No | No | No | Email address (added later) |
| username | VARCHAR(50) | No | No | No | No | Login username (unique) |
| password | VARCHAR(255) | No | No | No | No | Password (should be hashed) |
| role | ENUM | No | No | No | No | User role: 'owner', 'admin', 'coach' |
| security_question | VARCHAR(255) | Yes | No | No | No | Security question for password recovery |
| security_answer | VARCHAR(255) | Yes | No | No | No | Answer to security question |
| created_at | TIMESTAMP | No | No | No | No | Record creation timestamp |

#### Keys & Constraints

- **Primary Key**: `id`
- **Unique Constraint**: `username`
- **Default Values**:
  - `role`: 'admin'
  - `created_at`: CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | id | Primary key index |
| UNIQUE | username | Unique username constraint |
| idx_username | username | Authentication queries |
| idx_email | email | Email lookup |

#### Relationships

- **Standalone**: No foreign key relationships (legacy table)

#### Data Integrity Notes

- **Legacy Support**: Maintained for backward compatibility with old UI
- **Migration Path**: Users should be migrated to `users` table
- **Security Warning**: Original implementation used plain text passwords (should be hashed)

#### Table Dependencies

- **Depends On**: None
- **Depended By**: None

---

### 8. add_member (LEGACY)

**Purpose**: Legacy member table for old Add_Member.java interface.

**Module**: Legacy Member Management

**Status**: DEPRECATED - Use `members` table for new development

#### Column Details

| Column Name | Data Type | Nullable | Primary Key | Foreign Key | Auto-Increment | Description |
|-------------|-----------|----------|-------------|-------------|----------------|-------------|
| ID_NUM | VARCHAR(50) | No | Yes | No | No | Member identification number |
| FIRST_NAME | VARCHAR(100) | No | No | No | No | Member's first name |
| LAST_NAME | VARCHAR(100) | No | No | No | No | Member's last name |
| MOBILE_NUM | VARCHAR(20) | No | No | No | No | Mobile phone number |
| HEIGHT | DECIMAL(5,2) | Yes | No | No | No | Height in centimeters |
| WEIGHT | DECIMAL(5,2) | Yes | No | No | No | Weight in kilograms |
| GENDER | VARCHAR(10) | Yes | No | No | No | Gender |
| PERIOD_INFO | VARCHAR(255) | Yes | No | No | No | Subscription period description |
| PAYMENT | DECIMAL(10,2) | Yes | No | No | No | Payment amount |
| NUM_MEMB | INT | No | No | No | No | Member number |
| START_DATE | DATE | Yes | No | No | No | Membership start date |
| END_DATE | DATE | Yes | No | No | No | Membership expiration date |
| created_at | TIMESTAMP | No | No | No | No | Record creation timestamp |

#### Keys & Constraints

- **Primary Key**: `ID_NUM`
- **Default Values**:
  - `created_at`: CURRENT_TIMESTAMP

#### Indexes

| Index Name | Column(s) | Purpose |
|------------|-----------|---------|
| PRIMARY | ID_NUM | Primary key index |

#### Relationships

- **Standalone**: No foreign key relationships (legacy table)

#### Data Integrity Notes

- **Legacy Support**: Maintained for backward compatibility with old Add_Member.java
- **Migration Path**: Data should be migrated to `members` table
- **Naming Convention**: Uses UPPERCASE naming (different from modern schema)

#### Table Dependencies

- **Depends On**: None
- **Depended By**: None

---

## Database Views

### view_active_members

**Purpose**: Displays active members with coach and branch information, including membership status.

**Columns**:
- member_id
- random_id
- member_name (concatenated first_name + last_name)
- mobile
- email
- branch_name
- coach_name (concatenated coach first_name + last_name)
- start_date
- end_date
- payment
- days_remaining (calculated)
- membership_status (EXPIRED, EXPIRING_SOON, ACTIVE)

**Use Case**: Dashboard displays, member management interfaces

---

### view_revenue_by_branch

**Purpose**: Aggregates revenue statistics by branch.

**Columns**:
- branch_id
- branch_name
- total_members (count)
- active_members (count)
- total_revenue (sum of payments)
- avg_payment (average payment amount)

**Use Case**: Financial reports, branch performance analysis

---

### view_coach_workload

**Purpose**: Shows coach assignment statistics and workload distribution.

**Columns**:
- user_id
- coach_name (concatenated)
- branch_name
- assigned_members (total count)
- active_assigned (active members count)

**Use Case**: Coach workload balancing, resource allocation

---

## Stored Procedures

### sp_get_branch_statistics

**Parameters**:
- IN p_branch_id (INT)

**Purpose**: Retrieves comprehensive statistics for a specific branch.

**Returns**:
- total_members
- active_members
- expired_members
- total_revenue
- average_payment
- earliest_membership
- latest_expiry

**Use Case**: Branch performance reports, management dashboards

---

### sp_generate_random_id

**Parameters**:
- OUT p_random_id (INT)

**Purpose**: Generates a unique 8-digit random member ID.

**Logic**:
1. Generates random number between 10000000 and 99999999
2. Checks if number already exists in members table
3. Repeats until unique number found
4. Returns unique random ID

**Use Case**: Member registration process

---

## Triggers

### trg_member_status_update

**Event**: BEFORE UPDATE on `members` table

**Purpose**: Automatically sets `is_active` to FALSE when membership expires.

**Logic**:
```sql
IF NEW.end_date < CURDATE() THEN
    SET NEW.is_active = FALSE;
END IF;
```

**Use Case**: Automatic membership status management

---

## Schema Naming Conventions

### Table Naming

- **Style**: Lowercase, plural nouns
- **Pattern**: `noun_plural` or `noun`
- **Examples**: `branches`, `users`, `members`
- **Exceptions**: 
  - `regist` (legacy table, singular)
  - `add_member` (legacy table, snake_case)

### Column Naming

- **Style**: snake_case (lowercase with underscores)
- **Pattern**: `descriptive_name`
- **Examples**: `branch_id`, `first_name`, `created_at`
- **Conventions**:
  - Primary keys: `table_name_id` (e.g., `branch_id`, `user_id`)
  - Foreign keys: `referenced_table_id` (e.g., `branch_id`, `coach_id`)
  - Timestamps: `*_at` suffix (e.g., `created_at`, `updated_at`)
  - Boolean flags: `is_*` prefix (e.g., `is_active`)
  - Dates: `*_date` suffix (e.g., `start_date`, `end_date`)

### Index Naming

- **Pattern**: `idx_column_name` or `idx_composite_description`
- **Examples**: `idx_username`, `idx_branch_role`

### Constraint Naming

- **Foreign Keys**: Implicit naming by MySQL
- **Unique Constraints**: Implicit on UNIQUE columns

---

## Data Integrity Rules

### Referential Integrity

#### ON DELETE Rules

| Parent Table | Child Table | Foreign Key | On Delete Action | Rationale |
|--------------|-------------|-------------|------------------|-----------|
| branches | users | branch_id | SET NULL | User remains if branch deleted (for audit) |
| branches | members | assigned_branch | RESTRICT | Cannot delete branch with members |
| users | members | assigned_coach | SET NULL | Member remains if coach deleted |
| users | audit_logs | user_id | SET NULL | Preserve audit trail if user deleted |
| users | system_settings | updated_by | SET NULL | Preserve settings if user deleted |
| members | training_progress | member_id | CASCADE | Delete progress if member deleted |
| users | training_progress | coach_id | CASCADE | Delete progress if coach deleted |

#### ON UPDATE Rules

- **All foreign keys**: Default behavior (no explicit ON UPDATE)
- **Timestamps**: Automatic update via `ON UPDATE CURRENT_TIMESTAMP`

### Cascade Effects

1. **Deleting a Branch**:
   - Users: `branch_id` set to NULL (users preserved)
   - Members: DELETE RESTRICTED (must reassign or delete members first)

2. **Deleting a User (Coach)**:
   - Members: `assigned_coach` set to NULL
   - Training Progress: All records CASCADE deleted
   - Audit Logs: `user_id` set to NULL (logs preserved)
   - System Settings: `updated_by` set to NULL

3. **Deleting a Member**:
   - Training Progress: All records CASCADE deleted

### Data Validation

#### Check Constraints

- `training_progress.rating`: Must be between 1 and 5
- `members.end_date`: Validated by trigger (sets is_active to FALSE if expired)

#### Unique Constraints

- `branches.branch_name`: Must be unique
- `users.username`: Must be unique
- `users.email`: Must be unique
- `members.random_id`: Must be unique (8-digit card number)
- `system_settings.setting_key`: Must be unique
- `regist.username`: Must be unique

#### Not Null Constraints

- All primary keys
- All foreign keys except those allowing NULL (branch_id for owner, etc.)
- Critical fields: usernames, passwords, names, payment amounts, dates

---

## Database Statistics (Initial Seed Data)

| Entity | Count | Notes |
|--------|-------|-------|
| Branches | 2 | Cairo, Benha |
| Users | 7 | 1 owner, 2 admins, 4 coaches |
| Members | 12 | 6 per branch (Cairo, Benha) |
| Training Progress | 7 | Sample training sessions |
| Audit Logs | 5 | Sample audit entries |
| System Settings | 6 | Basic configuration |
| Regist (Legacy) | 3 | Legacy test accounts |

---

## Security Notes

### Password Storage

- **Requirement**: All passwords MUST be hashed
- **Recommended Algorithm**: BCrypt with cost factor 12+
- **Alternative**: PBKDF2 with sufficient iterations
- **Storage Length**: VARCHAR(255) supports BCrypt and future algorithms
- **Plain Text**: NEVER store plain text passwords

### Sensitive Data

- **Passwords**: Always hashed
- **Security Answers**: Should be hashed (currently plain text in regist table)
- **Email Credentials**: Stored in `system_settings` (should be encrypted at application level)
- **Audit Logs**: Preserved even when users deleted (compliance requirement)

### Access Control

- **Role Hierarchy**: owner > admin > coach
- **Branch Isolation**: Admins and coaches restricted to their assigned branch
- **Owner Access**: NULL branch_id indicates system-wide access

---

## Migration & Maintenance Notes

### Legacy Tables

Two legacy tables exist for backward compatibility:
1. **regist**: Old user authentication table (use `users` instead)
2. **add_member**: Old member management table (use `members` instead)

**Migration Strategy**:
- Maintain legacy tables during transition period
- Implement dual-write strategy for new data
- Migrate legacy data to new schema
- Deprecate legacy tables after full migration

### Schema Evolution

- **Character Set**: utf8mb4 supports Arabic, emoji, and international characters
- **Timestamps**: All tables include created_at and updated_at for audit trail
- **Soft Deletes**: Not implemented (uses foreign key constraints instead)
- **Versioning**: No built-in versioning (consider adding version column if needed)

---

## Documentation Metadata

**Last Updated**: December 16, 2025  
**Database Version**: 1.0  
**MySQL Version**: 8.0+  
**Reviewed By**: Database Documentation Specialist  
**Next Review Date**: Quarterly or after schema changes

---

*End of Database Schema Documentation*
