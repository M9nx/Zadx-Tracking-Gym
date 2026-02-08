package app.dao;

import app.util.DatabaseUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * System Settings DAO - Data access for system_settings table.
 * 
 * Provides key-value configuration storage for system-wide settings.
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class SystemSettingsDAO {
    
    private final DatabaseUtil dbUtil;
    
    public SystemSettingsDAO() {
        this.dbUtil = DatabaseUtil.getInstance();
    }
    
    /**
     * Gets a setting value by key.
     */
    public String get(String settingKey) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, settingKey);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting setting: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Sets a setting value by key. Creates if doesn't exist, updates if exists.
     */
    public boolean set(String settingKey, String settingValue) {
        // Check if exists
        String existing = get(settingKey);
        
        if (existing == null) {
            // Insert new
            String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?)";
            
            try (Connection conn = dbUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, settingKey);
                stmt.setString(2, settingValue);
                
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException e) {
                System.err.println("Error inserting setting: " + e.getMessage());
                return false;
            }
        } else {
            // Update existing
            String sql = "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?";
            
            try (Connection conn = dbUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, settingValue);
                stmt.setString(2, settingKey);
                
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException e) {
                System.err.println("Error updating setting: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Deletes a setting by key.
     */
    public boolean delete(String settingKey) {
        String sql = "DELETE FROM system_settings WHERE setting_key = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, settingKey);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting setting: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all settings as a map.
     */
    public Map<String, String> getAll() {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM system_settings";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all settings: " + e.getMessage());
        }
        
        return settings;
    }
}
