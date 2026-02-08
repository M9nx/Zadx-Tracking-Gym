package app.dao;

import app.model.User;
import app.model.UserRole;
import app.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO - Data Access Object for User operations
 * Handles all database operations for users table
 */
public class UserDAO {
    
    private final DatabaseUtil dbUtil;
    
    public UserDAO() {
        this.dbUtil = DatabaseUtil.getInstance();
    }
    
    /**
     * Find user by username
     * @param username Username to search for
     * @return Optional containing user if found
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Create new user
     */
    public boolean create(User user) {
        String sql = "INSERT INTO users (username, password, first_name, last_name, email, mobile, " +
                    "role, branch_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getMobile());
            stmt.setString(7, user.getRole().getDbValue());
            
            if (user.getBranchId() != null) {
                stmt.setInt(8, user.getBranchId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            
            stmt.setBoolean(9, user.isActive());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update user
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, first_name = ?, last_name = ?, " +
                    "email = ?, role = ?, branch_id = ?, is_active = ? WHERE user_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole().getDbValue());
            
            if (user.getBranchId() != null) {
                stmt.setInt(6, user.getBranchId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setBoolean(7, user.isActive());
            stmt.setInt(8, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update user password
     */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update last login time
     */
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete user
     */
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find all users by role
     */
    public List<User> findByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY first_name, last_name";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.getDbValue());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding users by role: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Find all users by branch
     */
    public List<User> findByBranch(int branchId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE branch_id = ? ORDER BY role, first_name";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding users by branch: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Find coaches by branch
     */
    public List<User> findCoachesByBranch(int branchId) {
        List<User> coaches = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'coach' AND branch_id = ? " +
                    "AND is_active = true ORDER BY first_name";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                coaches.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding coaches: " + e.getMessage());
        }
        
        return coaches;
    }
    
    /**
     * Find all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, first_name";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Find users by role and branch
     */
    public List<User> findByRoleAndBranch(UserRole role, Integer branchId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? AND branch_id = ? ORDER BY first_name";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.getDbValue());
            
            if (branchId != null) {
                stmt.setInt(2, branchId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding users by role and branch: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Integer branchId = rs.getInt("branch_id");
        if (rs.wasNull()) {
            branchId = null;
        }
        
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        LocalDateTime lastLogin = lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null;
        
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("mobile"),
            UserRole.fromString(rs.getString("role")),
            branchId,
            rs.getBoolean("is_active"),
            lastLogin,
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
