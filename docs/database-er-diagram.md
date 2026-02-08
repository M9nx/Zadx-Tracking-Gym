# Database Entity-Relationship Diagram (Text Format)

## Visual Schema Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        GYM MANAGEMENT SYSTEM DATABASE                        │
│                                   Schema: gymm                               │
└─────────────────────────────────────────────────────────────────────────────┘


                                    ┌──────────────┐
                                    │   branches   │
                                    ├──────────────┤
                                    │ PK branch_id │
                                    │ UQ branch_name
                                    │    address   │
                                    │    phone     │
                                    │    timestamps│
                                    └──────┬───────┘
                                           │
                      ┌────────────────────┴────────────────────┐
                      │                                         │
                      │ 1:M                                     │ 1:M
                      ▼                                         ▼
            ┌─────────────────┐                      ┌──────────────────┐
            │      users      │                      │     members      │
            ├─────────────────┤                      ├──────────────────┤
            │ PK  user_id     │                      │ PK  member_id    │
            │ UQ  username    │◄────────────────────┐│ UQ  random_id    │
            │ UQ  email       │                  M:1 ││     first_name   │
            │     password    │                     │││     last_name    │
            │     first_name  │                     │││     mobile       │
            │     last_name   │                     │││     email        │
            │     mobile      │                     │││     height       │
            │ ENUM role       │                     │││     weight       │
            │ FK  branch_id ──┘                     │││ ENUM gender      │
            │     is_active   │                     │││     dob          │
            │     last_login  │                     │││     payment      │
            │     timestamps  │                     │││     start_date   │
            └────┬───┬────────┘                     │││     end_date     │
                 │   │                              │││     period       │
                 │   │                              ││ FK  assigned_branch
                 │   │                              ││ FK  assigned_coach──┘
                 │   │                              ││     is_active    │
                 │   │                              ││     notes        │
                 │   │                              ││     timestamps   │
                 │   │                              │└──────┬───────────┘
                 │   │                              │       │
                 │   │ 1:M                          │ 1:M   │ 1:M
                 │   └──────────────────────────────┤       │
                 │                                  │       │
                 │                                  ▼       ▼
                 │                          ┌──────────────────────┐
                 │                          │  training_progress   │
                 │                          ├──────────────────────┤
                 │                          │ PK  progress_id      │
                 │                          │ FK  member_id ───────┘
                 │                          │ FK  coach_id         │
                 │                          │     session_date     │
                 │                          │     notes            │
                 │                          │     rating (1-5)     │
                 │                          │     timestamps       │
                 │                          └──────────────────────┘
                 │
                 │ 1:M
                 ├─────────────────────────┐
                 │                         │
                 ▼                         ▼
       ┌─────────────────┐      ┌──────────────────┐
       │   audit_logs    │      │ system_settings  │
       ├─────────────────┤      ├──────────────────┤
       │ PK  audit_id    │      │ PK  setting_id   │
       │ FK  user_id     │      │ UQ  setting_key  │
       │     action      │      │     setting_value│
       │     target_id   │      │     description  │
       │     target_type │      │ FK  updated_by   │
       │     details     │      │     updated_at   │
       │     ip_address  │      └──────────────────┘
       │     timestamp   │
       └─────────────────┘


═════════════════════════════════════════════════════════════════════════════
                              LEGACY TABLES (Deprecated)
═════════════════════════════════════════════════════════════════════════════

       ┌─────────────────┐              ┌──────────────────┐
       │  regist (OLD)   │              │ add_member (OLD) │
       ├─────────────────┤              ├──────────────────┤
       │ PK  id          │              │ PK  ID_NUM       │
       │ UQ  username    │              │     FIRST_NAME   │
       │     password    │              │     LAST_NAME    │
       │     first_name  │              │     MOBILE_NUM   │
       │     last_name   │              │     HEIGHT       │
       │     email       │              │     WEIGHT       │
       │ ENUM role       │              │     GENDER       │
       │     sec_question│              │     PERIOD_INFO  │
       │     sec_answer  │              │     PAYMENT      │
       │     created_at  │              │     NUM_MEMB     │
       └─────────────────┘              │     START_DATE   │
                                        │     END_DATE     │
                                        │     created_at   │
                                        └──────────────────┘

```

---

## Relationship Matrix

| From Table | From Column | To Table | To Column | Relationship | On Delete | On Update |
|------------|-------------|----------|-----------|--------------|-----------|-----------|
| **branches** | branch_id | users | branch_id | 1:M | SET NULL | - |
| **branches** | branch_id | members | assigned_branch | 1:M | RESTRICT | - |
| **users** | user_id | members | assigned_coach | 1:M | SET NULL | - |
| **users** | user_id | training_progress | coach_id | 1:M | CASCADE | - |
| **users** | user_id | audit_logs | user_id | 1:M | SET NULL | - |
| **users** | user_id | system_settings | updated_by | 1:M | SET NULL | - |
| **members** | member_id | training_progress | member_id | 1:M | CASCADE | - |

---

## Cardinality Summary

### One-to-Many Relationships

```
branches (1) ──────────< users (M)
    Constraint: users.branch_id → branches.branch_id
    On Delete: SET NULL (owner has NULL branch_id)
    
branches (1) ──────────< members (M)
    Constraint: members.assigned_branch → branches.branch_id
    On Delete: RESTRICT (cannot delete branch with members)
    
users (1) ─────────────< members (M)
    Constraint: members.assigned_coach → users.user_id
    On Delete: SET NULL (member remains if coach deleted)
    
users (1) ─────────────< training_progress (M)
    Constraint: training_progress.coach_id → users.user_id
    On Delete: CASCADE (delete progress if coach deleted)
    
members (1) ───────────< training_progress (M)
    Constraint: training_progress.member_id → members.member_id
    On Delete: CASCADE (delete progress if member deleted)
    
users (1) ─────────────< audit_logs (M)
    Constraint: audit_logs.user_id → users.user_id
    On Delete: SET NULL (preserve audit log)
    
users (1) ─────────────< system_settings (M)
    Constraint: system_settings.updated_by → users.user_id
    On Delete: SET NULL (preserve settings)
```

---

## Table Hierarchy

```
Level 0 (Independent):
    └── branches
        │
        ├── Level 1 (Depends on branches):
        │   └── users
        │       │
        │       └── Level 2 (Depends on users and branches):
        │           ├── members
        │           ├── audit_logs
        │           └── system_settings
        │               │
        │               └── Level 3 (Depends on members and users):
        │                   └── training_progress
```

---

## Data Flow Diagram

```
┌─────────┐
│ OWNER   │ (NULL branch_id, system-wide access)
└────┬────┘
     │
     ├─── Creates/Manages ──► ┌──────────┐
     │                        │ Branches │
     │                        └─────┬────┘
     │                              │
     │                              ├─── Has ──► ┌────────┐
     │                              │            │ Admins │
     │                              │            └────┬───┘
     │                              │                 │
     │                              └─── Has ──► ┌────────┐
     │                                           │ Coaches│
     │                                           └───┬────┘
     │                                               │
     │                                               ├─── Manages ──► ┌─────────┐
     │                                               │                 │ Members │
     │                                               │                 └────┬────┘
     │                                               │                      │
     │                                               └─── Records ──► ┌──────────┐
     │                                                                 │ Training │
     │                                                                 │ Progress │
     │                                                                 └──────────┘
     │
     └─── All actions logged ──► ┌─────────────┐
                                  │ Audit Logs  │
                                  └─────────────┘
```

---

## Index Coverage Map

### Highly Indexed Tables (Performance Critical)

**members** (10 indexes):
- Primary key: member_id
- Unique: random_id
- Single column: mobile, branch, coach, active, dates
- Composite: (branch, active, end_date), (coach, active, end_date)

**users** (6 indexes):
- Primary key: user_id
- Unique: username, email
- Single column: username, role
- Composite: (branch, role)

**audit_logs** (5 indexes):
- Primary key: audit_id
- Single column: timestamp, action
- Composite: (user, action), (user, timestamp)

---

## Query Optimization Guide

### Common Queries and Their Indexed Paths

| Query Pattern | Indexed Columns | Performance |
|---------------|----------------|-------------|
| Login authentication | users.username | ✓ Unique index |
| Member search by phone | members.mobile | ✓ Indexed |
| Member card validation | members.random_id | ✓ Unique index |
| Branch member list | members.(branch, active, end_date) | ✓ Composite |
| Coach workload | members.(coach, active, end_date) | ✓ Composite |
| Audit trail by user | audit_logs.(user, timestamp) | ✓ Composite |
| Expired memberships | members.(active, end_date) | ✓ Indexed |
| Settings lookup | system_settings.setting_key | ✓ Unique index |

---

## Database Size Estimates

### Storage Requirements (Estimates)

| Table | Row Size (avg) | 100 Members | 1,000 Members | 10,000 Members |
|-------|---------------|-------------|---------------|----------------|
| branches | 500 bytes | 1 KB | 1 KB | 1 KB |
| users | 600 bytes | 60 KB | 600 KB | 6 MB |
| members | 800 bytes | 80 KB | 800 KB | 8 MB |
| training_progress | 400 bytes | 160 KB | 1.6 MB | 16 MB |
| audit_logs | 300 bytes | 120 KB | 1.2 MB | 12 MB |
| system_settings | 200 bytes | 2 KB | 2 KB | 2 KB |
| **Total** | - | ~423 KB | ~4.2 MB | ~42 MB |

*Note: Excludes indexes (typically 20-30% additional storage)*

---

## Maintenance Windows

### Recommended Maintenance Tasks

| Task | Frequency | Impact | Downtime Required |
|------|-----------|--------|-------------------|
| Analyze tables | Weekly | Low | No |
| Optimize tables | Monthly | Medium | Minimal |
| Backup | Daily | None | No (online backup) |
| Archive old audit_logs | Quarterly | Low | No |
| Index rebuild | Yearly | Medium | Planned (off-hours) |
| Update statistics | Weekly | Low | No |

---

## Backup Strategy

### Recommended Approach

1. **Daily Full Backup**: Complete database dump
2. **Hourly Incremental**: Binary log backups
3. **Critical Tables** (real-time replication):
   - members
   - training_progress
   - audit_logs
4. **Recovery Point Objective (RPO)**: < 1 hour
5. **Recovery Time Objective (RTO)**: < 30 minutes

---

*End of ER Diagram Documentation*
