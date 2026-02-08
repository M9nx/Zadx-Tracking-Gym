package app.dao;

import app.model.TrainingProgress;
import app.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Training Progress DAO - Data access for training_progress table.
 * 
 * Provides CRUD operations for tracking member training sessions.
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class TrainingProgressDAO {
    
    private final DatabaseUtil dbUtil;
    
    public TrainingProgressDAO() {
        this.dbUtil = DatabaseUtil.getInstance();
    }
    
    /**
     * Creates a new training progress record.
     */
    public boolean create(TrainingProgress progress) {
        String sql = "INSERT INTO training_progress (member_id, coach_id, session_date, notes, rating) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, progress.getMemberId());
            stmt.setInt(2, progress.getCoachId());
            stmt.setDate(3, Date.valueOf(progress.getSessionDate()));
            stmt.setString(4, progress.getNotes());
            
            if (progress.getRating() != null) {
                stmt.setInt(5, progress.getRating());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating training progress: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Finds a training progress record by ID.
     */
    public Optional<TrainingProgress> findById(int id) {
        String sql = "SELECT * FROM training_progress WHERE id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTrainingProgress(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding training progress by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all training progress records for a specific member.
     */
    public List<TrainingProgress> findByMember(int memberId) {
        String sql = "SELECT * FROM training_progress WHERE member_id = ? ORDER BY session_date DESC";
        List<TrainingProgress> progressList = new ArrayList<>();
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    progressList.add(mapResultSetToTrainingProgress(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding training progress by member: " + e.getMessage());
        }
        
        return progressList;
    }
    
    /**
     * Finds all training progress records created by a specific coach.
     */
    public List<TrainingProgress> findByCoach(int coachId) {
        String sql = "SELECT * FROM training_progress WHERE coach_id = ? ORDER BY session_date DESC";
        List<TrainingProgress> progressList = new ArrayList<>();
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, coachId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    progressList.add(mapResultSetToTrainingProgress(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding training progress by coach: " + e.getMessage());
        }
        
        return progressList;
    }
    
    /**
     * Updates an existing training progress record.
     */
    public boolean update(TrainingProgress progress) {
        String sql = "UPDATE training_progress SET notes = ?, rating = ? WHERE id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, progress.getNotes());
            
            if (progress.getRating() != null) {
                stmt.setInt(2, progress.getRating());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setInt(3, progress.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating training progress: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a training progress record.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM training_progress WHERE id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting training progress: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Maps a ResultSet row to a TrainingProgress object.
     */
    private TrainingProgress mapResultSetToTrainingProgress(ResultSet rs) throws SQLException {
        Integer rating = rs.getInt("rating");
        if (rs.wasNull()) {
            rating = null;
        }
        
        return new TrainingProgress(
            rs.getInt("id"),
            rs.getInt("member_id"),
            rs.getInt("coach_id"),
            rs.getDate("session_date").toLocalDate(),
            rs.getString("notes"),
            rating,
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
