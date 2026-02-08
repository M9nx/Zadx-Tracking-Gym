package app.service;

import app.dao.AuditLogDAO;
import app.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Service - Business logic for audit logging and retrieval.
 * 
 * This service provides:
 * - Simplified audit log creation methods
 * - Audit log search and filtering
 * - Pre-defined action constants
 * - Common audit logging patterns
 * 
 * Purpose:
 * Audit logs track all sensitive operations in the system including:
 * - User authentication (login, logout, password changes)
 * - User management (create, update, delete users)
 * - Member management (create, update, delete members)
 * - Branch management
 * - System configuration changes
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class AuditService {
    
    private final AuditLogDAO auditLogDAO;
    
    /**
     * Constructs the AuditService.
     */
    public AuditService() {
        this.auditLogDAO = new AuditLogDAO();
    }
    
    /**
     * Creates a general audit log entry.
     * 
     * @param userId the ID of the user performing the action (null for system actions)
     * @param action the action being performed
     * @param details additional details about the action
     * @param ipAddress the IP address of the request
     * @return true if log was created successfully, false otherwise
     */
    public boolean log(Integer userId, String action, String details, String ipAddress) {
        return auditLogDAO.create(userId, action, details, ipAddress);
    }
    
    /**
     * Logs a successful login.
     */
    public boolean logLoginSuccess(int userId, String username, String ipAddress) {
        return auditLogDAO.create(
            userId,
            AuditLog.Actions.LOGIN_SUCCESS,
            "User logged in: " + username,
            ipAddress
        );
    }
    
    /**
     * Logs a failed login attempt.
     */
    public boolean logLoginFailed(String username, String reason, String ipAddress) {
        return auditLogDAO.create(
            null,
            AuditLog.Actions.LOGIN_FAILED,
            "Failed login attempt for username: " + username + " - Reason: " + reason,
            ipAddress
        );
    }
    
    /**
     * Logs a logout action.
     */
    public boolean logLogout(int userId, String username, String ipAddress) {
        return auditLogDAO.create(
            userId,
            AuditLog.Actions.LOGOUT,
            "User logged out: " + username,
            ipAddress
        );
    }
    
    /**
     * Logs a password change.
     */
    public boolean logPasswordChange(int userId, String username, String ipAddress) {
        return auditLogDAO.create(
            userId,
            AuditLog.Actions.PASSWORD_CHANGE_SUCCESS,
            "User changed password: " + username,
            ipAddress
        );
    }
    
    /**
     * Logs a password reset.
     */
    public boolean logPasswordReset(int userId, String username, String ipAddress) {
        return auditLogDAO.create(
            userId,
            AuditLog.Actions.PASSWORD_RESET_SUCCESS,
            "Password reset for user: " + username,
            ipAddress
        );
    }
    
    /**
     * Logs user creation.
     */
    public boolean logUserCreate(int creatorId, String newUsername, String role, String ipAddress) {
        return auditLogDAO.create(
            creatorId,
            AuditLog.Actions.USER_CREATE,
            "Created new user: " + newUsername + " (Role: " + role + ")",
            ipAddress
        );
    }
    
    /**
     * Logs user update.
     */
    public boolean logUserUpdate(int updaterId, String username, String ipAddress) {
        return auditLogDAO.create(
            updaterId,
            AuditLog.Actions.USER_UPDATE,
            "Updated user: " + username,
            ipAddress
        );
    }
    
    /**
     * Logs user deletion.
     */
    public boolean logUserDelete(int deleterId, String username, String ipAddress) {
        return auditLogDAO.create(
            deleterId,
            AuditLog.Actions.USER_DELETE,
            "Deleted user: " + username,
            ipAddress
        );
    }
    
    /**
     * Logs member creation.
     */
    public boolean logMemberCreate(int creatorId, String memberName, String randomId, String ipAddress) {
        return auditLogDAO.create(
            creatorId,
            AuditLog.Actions.MEMBER_CREATE,
            "Created new member: " + memberName + " (ID: " + randomId + ")",
            ipAddress
        );
    }
    
    /**
     * Logs member update.
     */
    public boolean logMemberUpdate(int updaterId, String memberName, String randomId, String ipAddress) {
        return auditLogDAO.create(
            updaterId,
            AuditLog.Actions.MEMBER_UPDATE,
            "Updated member: " + memberName + " (ID: " + randomId + ")",
            ipAddress
        );
    }
    
    /**
     * Logs member deletion.
     */
    public boolean logMemberDelete(int deleterId, String memberName, String randomId, String ipAddress) {
        return auditLogDAO.create(
            deleterId,
            AuditLog.Actions.MEMBER_DELETE,
            "Deleted member: " + memberName + " (ID: " + randomId + ")",
            ipAddress
        );
    }
    
    /**
     * Logs branch creation.
     */
    public boolean logBranchCreate(int creatorId, String branchName, String ipAddress) {
        return auditLogDAO.create(
            creatorId,
            AuditLog.Actions.BRANCH_CREATE,
            "Created new branch: " + branchName,
            ipAddress
        );
    }
    
    /**
     * Logs branch update.
     */
    public boolean logBranchUpdate(int updaterId, String branchName, String ipAddress) {
        return auditLogDAO.create(
            updaterId,
            AuditLog.Actions.BRANCH_UPDATE,
            "Updated branch: " + branchName,
            ipAddress
        );
    }
    
    /**
     * Logs branch deletion.
     */
    public boolean logBranchDelete(int deleterId, String branchName, String ipAddress) {
        return auditLogDAO.create(
            deleterId,
            AuditLog.Actions.BRANCH_DELETE,
            "Deleted branch: " + branchName,
            ipAddress
        );
    }
    
    /**
     * Logs training progress update.
     */
    public boolean logTrainingUpdate(int coachId, String memberName, String ipAddress) {
        return auditLogDAO.create(
            coachId,
            AuditLog.Actions.TRAINING_UPDATE,
            "Updated training progress for member: " + memberName,
            ipAddress
        );
    }
    
    /**
     * Logs system settings change.
     */
    public boolean logSettingsChange(int userId, String settingName, String ipAddress) {
        return auditLogDAO.create(
            userId,
            AuditLog.Actions.SETTINGS_CHANGE,
            "Changed system setting: " + settingName,
            ipAddress
        );
    }
    
    /**
     * Searches audit logs with optional filters.
     * 
     * @param userId filter by user ID (null for all users)
     * @param action filter by action type (null for all actions)
     * @param startDate filter by start date (null for no start date filter)
     * @param endDate filter by end date (null for no end date filter)
     * @return list of matching audit logs
     */
    public List<AuditLog> searchLogs(
            Integer userId,
            String action,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        return auditLogDAO.search(userId, action, startDate, endDate, 1000);
    }
    
    /**
     * Gets all audit logs for a specific user.
     */
    public List<AuditLog> getLogsByUser(int userId) {
        return auditLogDAO.search(userId, null, null, null, 1000);
    }
    
    /**
     * Gets all audit logs for a specific action type.
     */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogDAO.search(null, action, null, null, 1000);
    }
    
    /**
     * Gets all audit logs within a date range.
     */
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogDAO.search(null, null, startDate, endDate, 1000);
    }
}
