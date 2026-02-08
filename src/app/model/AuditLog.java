package app.model;

import java.time.LocalDateTime;

/**
 * AuditLog Model - Represents system audit log entries
 * Immutable DTO for audit log data
 */
public class AuditLog {
    private final long auditId;
    private final Integer userId; // Nullable for system events
    private final String action;
    private final Integer targetId; // Nullable
    private final String targetType; // Nullable
    private final String details; // JSON or plain text
    private final String ipAddress; // Nullable
    private final LocalDateTime timestamp;

    // Constructor for new audit logs (without ID)
    public AuditLog(Integer userId, String action, Integer targetId,
                   String targetType, String details, String ipAddress) {
        this(0, userId, action, targetId, targetType, details, ipAddress, null);
    }

    // Full constructor (from database)
    public AuditLog(long auditId, Integer userId, String action, Integer targetId,
                   String targetType, String details, String ipAddress,
                   LocalDateTime timestamp) {
        this.auditId = auditId;
        this.userId = userId;
        this.action = action;
        this.targetId = targetId;
        this.targetType = targetType;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Getters
    public long getAuditId() { return auditId; }
    public Integer getUserId() { return userId; }
    public String getAction() { return action; }
    public Integer getTargetId() { return targetId; }
    public String getTargetType() { return targetType; }
    public String getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return action + " at " + timestamp + " by user " + userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AuditLog auditLog = (AuditLog) obj;
        return auditId == auditLog.auditId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(auditId);
    }

    /**
     * Common audit action constants
     */
    public static class Actions {
        public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static final String LOGIN_FAILED = "LOGIN_FAILED"; // Added
        public static final String LOGIN_FAIL = "LOGIN_FAIL";
        public static final String LOGOUT = "LOGOUT";
        public static final String PASSWORD_RESET = "PASSWORD_RESET";
        public static final String PASSWORD_RESET_SUCCESS = "PASSWORD_RESET_SUCCESS"; // Added
        public static final String PASSWORD_CHANGE = "PASSWORD_CHANGE";
        public static final String PASSWORD_CHANGE_SUCCESS = "PASSWORD_CHANGE_SUCCESS"; // Added
        public static final String USER_CREATE = "USER_CREATE";
        public static final String USER_UPDATE = "USER_UPDATE";
        public static final String USER_DELETE = "USER_DELETE";
        public static final String MEMBER_CREATE = "MEMBER_CREATE";
        public static final String MEMBER_UPDATE = "MEMBER_UPDATE";
        public static final String MEMBER_DELETE = "MEMBER_DELETE";
        public static final String BRANCH_CREATE = "BRANCH_CREATE";
        public static final String BRANCH_UPDATE = "BRANCH_UPDATE";
        public static final String BRANCH_DELETE = "BRANCH_DELETE";
        public static final String TRAINING_CREATE = "TRAINING_CREATE";
        public static final String TRAINING_UPDATE = "TRAINING_UPDATE";
        public static final String REPORT_EXPORT = "REPORT_EXPORT";
        public static final String DATA_IMPORT = "DATA_IMPORT";
        public static final String BACKUP_CREATE = "BACKUP_CREATE";
        public static final String BACKUP_RESTORE = "BACKUP_RESTORE";
        public static final String SETTINGS_CHANGE = "SETTINGS_CHANGE"; // Added
    }
}
