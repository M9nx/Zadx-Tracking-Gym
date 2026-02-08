package app.dao;

import app.model.AuditLog;
import app.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDAO - Data Access Object for AuditLog operations
 */
public class AuditLogDAO {
    
    private final DatabaseUtil dbUtil;
    
    public AuditLogDAO() {
        this.dbUtil = DatabaseUtil.getInstance();
    }
    
    /**
     * Create audit log entry from individual parameters
     */
    public boolean create(Integer userId, String action, String details, String ipAddress) {
        String sql = "INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setString(4, ipAddress);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating audit log: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create audit log entry from AuditLog object
     */
    public boolean create(AuditLog auditLog) {
        String sql = "INSERT INTO audit_logs (user_id, action, target_id, target_type, " +
                    "details, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (auditLog.getUserId() != null) {
                stmt.setInt(1, auditLog.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, auditLog.getAction());
            
            if (auditLog.getTargetId() != null) {
                stmt.setInt(3, auditLog.getTargetId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setString(4, auditLog.getTargetType());
            stmt.setString(5, auditLog.getDetails());
            stmt.setString(6, auditLog.getIpAddress());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating audit log: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find logs by user ID
     */
    public List<AuditLog> findByUserId(int userId, int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding audit logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Find logs by action
     */
    public List<AuditLog> findByAction(String action, int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action = ? ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, action);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding audit logs by action: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Find recent logs
     */
    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding recent audit logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Search logs with filters
     */
    public List<AuditLog> search(Integer userId, String action, LocalDateTime fromDate, 
                                 LocalDateTime toDate, int limit) {
        List<AuditLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1");
        
        if (userId != null) sql.append(" AND user_id = ?");
        if (action != null && !action.isEmpty()) sql.append(" AND action = ?");
        if (fromDate != null) sql.append(" AND timestamp >= ?");
        if (toDate != null) sql.append(" AND timestamp <= ?");
        
        sql.append(" ORDER BY timestamp DESC LIMIT ?");
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (userId != null) stmt.setInt(paramIndex++, userId);
            if (action != null && !action.isEmpty()) stmt.setString(paramIndex++, action);
            if (fromDate != null) stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fromDate));
            if (toDate != null) stmt.setTimestamp(paramIndex++, Timestamp.valueOf(toDate));
            stmt.setInt(paramIndex, limit);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching audit logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        Integer userId = rs.getInt("user_id");
        if (rs.wasNull()) userId = null;
        
        Integer targetId = rs.getInt("target_id");
        if (rs.wasNull()) targetId = null;
        
        return new AuditLog(
            rs.getLong("audit_id"),
            userId,
            rs.getString("action"),
            targetId,
            rs.getString("target_type"),
            rs.getString("details"),
            rs.getString("ip_address"),
            rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
