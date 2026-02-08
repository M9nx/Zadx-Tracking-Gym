# Swing View Layer Documentation

_Date: 2025-12-16_

This document covers the Swing **View** layer only. Each entry lists purpose, UI/layout, user events, data/service dependencies, threading notes, reuse considerations, and alignment with Oracle Swing patterns.

## Legacy Frames (root `src`)

### Add_Member (JFrame)
- Purpose: Legacy member creation; inserts directly into `members` via JDBC.
- UI/Layout: GroupLayout form with text panes for name/contact/payment/dates, gender radio buttons, coach combo, buttons Save/Clear/Back.
- Events: Save validates minimal fields then executes SQL insert; Clear resets; Back disposes.
- Data/Services: Raw JDBC through `ConnectionProvider`.
- Threading: All work on EDT (risk of UI freeze during DB).
- Notes: Minimal validation; plain-text data; date parsing fragile.

### Coshes (JFrame)
- Purpose: Legacy coach CRUD.
- UI/Layout: GroupLayout form + JTable of coaches; buttons Add/Edit/Delete/Refresh.
- Events: Table click populates form; buttons run SQL insert/update/delete.
- Data/Services: Raw JDBC (`ConnectionProvider`).
- Threading: EDT for DB work.
- Notes: No validation; single-table model.

### LOGIN (JFrame)
- Purpose: Legacy login routing to dashboards or `Main_INTERFACE`.
- UI/Layout: Absolute layout with background; username/password fields; Login/Register/Exit/Forgot buttons.
- Events: Login queries `users` and `regist` tables; role-based navigation; forgot opens `Restore_Pass`; register opens `Register`.
- Data/Services: Raw JDBC.
- Threading: EDT only.
- Notes: Plain-text passwords; security gaps.

### Main_INTERFACE (JFrame)
- Purpose: Legacy menu hub.
- UI/Layout: Buttons to Add Member, Coaches, View Member, Logout, Exit; background image.
- Events: Buttons open corresponding legacy frames.
- Data/Services: None.
- Threading: N/A beyond EDT UI.

### Register (JFrame)
- Purpose: Legacy self-registration into `regist`.
- UI/Layout: GroupLayout text panes for username/name/password/retype; Register/Back buttons.
- Events: Register inserts on match; Back to login.
- Data/Services: Raw JDBC.
- Threading: EDT only.
- Notes: No hashing; minimal validation.

### Restore_Pass (JFrame)
- Purpose: Legacy password recovery via security answer.
- UI/Layout: GroupLayout fields username/answer; Retrieve/Register/Login buttons.
- Events: Retrieves and displays stored password if answer matches.
- Data/Services: Raw JDBC on `regist`.
- Threading: EDT only.
- Notes: Security risk (shows password directly).

### viiew_member (JFrame)
- Purpose: Legacy member view/edit/delete.
- UI/Layout: JTable plus form fields; buttons Add/Update/Delete/Exit.
- Events: Row click populates fields; Update/Delete execute SQL; Add opens `Add_Member`.
- Data/Services: Raw JDBC.
- Threading: EDT only.
- Notes: No validation; direct SQL.

### PasswordResetPage (JFrame)
- Purpose: Modern email-based password reset.
- UI/Layout: Glass/absolute layout; username/email/security answer fields; Send/Back; status labels.
- Events: Send uses `SwingWorker` to call `EmailService.sendResetEmail`; validates required fields.
- Data/Services: `UserService`, `EmailService`.
- Threading: Uses SwingWorker for background email send; UI updates on EDT.
- Notes: Modern styling via `UIThemeUtil`.

## Shared Modern Views (`src/app/views`)

### AuditLogsView (JPanel)
- Purpose: View audit logs with filters/search/limit.
- UI/Layout: Glass panel; header search/date/limit controls; JTable center.
- Events: Search/filter triggers DAO fetch; table read-only.
- Data/Services: `AuditLogDAO`, `UserDAO`.
- Threading: EDT DAO calls.

### LoginView (JFrame)
- Purpose: Modern login; routes by role; owner-only forgot password.
- UI/Layout: Gradient background; form with username/password; buttons Login/Forgot.
- Events: Login via `AuthenticationService.authenticate`; opens Owner/Admin/Coach dashboards; forgot triggers `sendPasswordReset` for owner.
- Data/Services: `AuthenticationService`, `SessionManager`.
- Threading: EDT calls.

## Admin Views (`src/app/views/admin`)

### AdminDashboard (JFrame)
- Purpose: Admin shell with navigation.
- UI/Layout: Sidebar buttons; main content swaps child panels.
- Events: Button actions swap to `ManageMembersView` (branch), `ManageCoachesView`, `BranchReportsView`, `AdminProfileView`; logout clears session.
- Data/Services: Delegated to children.
- Threading: EDT.

### AdminProfileView (JPanel)
- Purpose: Admin profile display; reset password; request branch transfer.
- UI/Layout: Read-only labels; buttons for reset and request.
- Events: Reset uses `EmailService.sendResetPassword`; transfer sends email to owner.
- Data/Services: `UserDAO`, `BranchDAO`, `EmailService`, `SessionManager`.
- Threading: EDT.

### BranchReportsView (JPanel)
- Purpose: Branch stats (members/revenue/coaches) for admin.
- UI/Layout: Gradient/card styling; header filters; JTable.
- Events: Refresh reruns query.
- Data/Services: Direct JDBC via `ConnectionProvider`.
- Threading: EDT (heavy queries on UI thread).

### ManageCoachesView (JPanel)
- Purpose: Coach management per branch.
- UI/Layout: Glass panel; header search; JTable; buttons Add/Edit/Delete/Refresh.
- Events: Add/Edit open `ViewEditUserDialog` (role Coach); Delete via `UserDAO.deleteUser`.
- Data/Services: `UserDAO`, `BranchDAO`, `SessionManager`.
- Threading: EDT.

## Coach Views (`src/app/views/coach`)

### CoachDashboard (JFrame)
- Purpose: Coach shell with navigation.
- UI/Layout: Sidebar; main panel swaps `CoachMembersView`, `TrainingProgressView`, `CoachProfileView`.
- Events: Buttons swap panels; logout clears session.
- Data/Services: Delegated to children.
- Threading: EDT.

### CoachMembersView (JPanel)
- Purpose: Read-only list of coach-assigned members with summary cards.
- UI/Layout: Glass cards; search; JTable.
- Events: Search reloads; refresh on load.
- Data/Services: JDBC via `ConnectionProvider`, `SessionManager`.
- Threading: EDT.

### CoachProfileView (JPanel)
- Purpose: Display coach profile; reset password via email.
- UI/Layout: Labels; reset button.
- Events: Reset calls `EmailService.sendResetPassword`.
- Data/Services: `UserDAO`, `BranchDAO`, `EmailService`, `SessionManager`.
- Threading: EDT.

### TrainingProgressView (JPanel)
- Purpose: Track training progress for members.
- UI/Layout: Members table left; progress table right; add-progress dialog.
- Events: Member selection loads progress; Add opens modal to insert note/date.
- Data/Services: JDBC (`ConnectionProvider`).
- Threading: EDT; no background threads.

## Owner Views (`src/app/views/owner`)

### OwnerDashboard (JFrame)
- Purpose: Owner shell navigating shared views.
- UI/Layout: Sidebar gradient; main content swaps `ManageBranchesView`, `ManageUsersView`, `ManageMembersView`, `StatisticsView`, `SystemSettingsView`, `AuditLogsView`.
- Events: Buttons swap panels; logout clears session.
- Data/Services: Delegated to children.
- Threading: EDT.

### SystemSettingsView (JPanel)
- Purpose: System-wide settings (general, business, payment, security, email, appearance, backup).
- UI/Layout: Gradient background; multiple section panels with rounded borders; Save/Reset/Test Email/Browse backup actions.
- Events: Save persists via `SystemSettingsDAO`; Reset reloads defaults; Test Email sends sample using background thread; Browse uses `JFileChooser`.
- Data/Services: `SystemSettingsDAO`, `AuditService`, `UIThemeUtil`, `SessionManager`.
- Threading: Test email uses background thread with `SwingUtilities.invokeLater` for UI updates; other operations on EDT.
- Notes: Includes `RoundedBorder` helper and Enter-key navigation helper.

## Shared Views (`src/app/views/shared`)

### ManageBranchesView (JPanel)
- Purpose: Owner branch list with counts and CRUD.
- UI/Layout: Glass panel; search; JTable (hidden ID); buttons Add/Edit/Delete/Refresh.
- Events: Search filters; Add/Edit open `ViewEditBranchDialog`; Delete checks member/coach counts then calls service.
- Data/Services: `BranchService`, `MemberService`, `UserService`.
- Threading: EDT.

### ManageMembersView (JPanel)
- Purpose: Member CRUD for owner/admin; read-only for coach.
- UI/Layout: Glass panel; search + status filter; JTable; buttons Add/Edit/Delete/Refresh.
- Events: Search via `memberService.searchMembers`; status filter reloads; Add/Edit open `ViewEditMemberDialog`; Delete calls service.
- Data/Services: `MemberService`, `UserService`, `SessionManager`.
- Threading: EDT.
- Notes: TODO for expiring-soon logic.

### ManageUsersView (JPanel)
- Purpose: User management (owner all, admin filtered); roles admin/coach.
- UI/Layout: Glass panel; search; JTable; buttons Add/Edit/Delete/Reset Password/Refresh.
- Events: Search client-side; Add/Edit open `ViewEditUserDialog`; Delete via `UserService.deleteUser`; Reset password placeholder dialog.
- Data/Services: `UserService`, `BranchService`, `SessionManager`.
- Threading: EDT.

### StatisticsView (JPanel)
- Purpose: System statistics with custom charts and export.
- UI/Layout: Glass panel; quarter selector; cards grid; multiple custom-painted charts; export buttons CSV/PDF; scrollable content.
- Events: Quarter change reloads stats; CSV writes file; PDF attempts print-to-PDF with HTML fallback.
- Data/Services: `MemberService`, `UserService`, `BranchService` for counts and payments.
- Threading: EDT; exports run on EDT.
- Notes: Charts manually painted; revenue estimates heuristic.

### ViewEditBranchDialog (JDialog)
- Purpose: Modal add/edit branch.
- UI/Layout: GridBag form; Active checkbox; Save/Cancel buttons.
- Events: Save validates and calls `BranchService` create/update.
- Data/Services: `BranchService`, `SessionManager`.
- Threading: EDT.

### ViewEditMemberDialog (JDialog)
- Purpose: Modal add/edit member with auto period/end-date.
- UI/Layout: Absolute layout; many fields; gender/coach combos; Active checkbox; Save/Cancel.
- Events: Payment/start-date key listeners compute period/end date; Save validates then create/update via `MemberService`.
- Data/Services: `MemberService`, `UserService`, `SessionManager`, `DateUtil`.
- Threading: EDT.
- Notes: Uses BigDecimal parsing; saved-flag for caller.

### ViewEditUserDialog (JDialog)
- Purpose: Modal add/edit user (admin/coach roles).
- UI/Layout: Absolute glass panel; fields username/name/email/mobile/passwords; role and branch combos; Active checkbox; Save/Cancel; password strength label.
- Events: Password key listener uses `PasswordUtil.validatePasswordComplexity`; Save validates required fields, password match/strength, branch requirement; calls `UserService` create/update.
- Data/Services: `UserService`, `BranchService`, `PasswordUtil`, `ValidationUtil`, `SessionManager`.
- Threading: EDT.
- Notes: Username immutable on edit; saved-flag for caller.

## Threading Snapshot
- Background use: `PasswordResetPage` (SwingWorker), `SystemSettingsView` (background thread + `invokeLater`).
- All other DAO/DB/email calls run on EDT; long operations can freeze UI.

## Oracle Swing Alignment
- Layouts: BorderLayout, FlowLayout, GridBagLayout, GroupLayout, BoxLayout, and some absolute layouts.
- Components: `JFrame`, `JPanel`, `JDialog`, `JTable` with `DefaultTableModel`, `JScrollPane`, `JButton`, `JTextField`/`JPasswordField`/`JTextPane`, `JComboBox`, `JCheckBox`, `JLabel`, `JFileChooser`, `JOptionPane`.
- Painting: Custom `paintComponent` for gradients/cards; `RoundedBorder` for rounded edges.
- Concurrency: Proper EDT updates; limited use of `SwingWorker`/threads as noted above.
