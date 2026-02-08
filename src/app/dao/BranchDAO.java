package app.dao;

import app.model.Branch;
import app.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BranchDAO - Data Access Object for Branch operations
 */
public class BranchDAO {
    
    private final DatabaseUtil dbUtil;
    
    public BranchDAO() {
        this.dbUtil = DatabaseUtil.getInstance();
    }
    
    public Optional<Branch> findById(int branchId) {
        String sql = "SELECT * FROM branches WHERE branch_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBranch(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding branch: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    public List<Branch> findAll() {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM branches ORDER BY branch_name";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                branches.add(mapResultSetToBranch(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all branches: " + e.getMessage());
        }
        
        return branches;
    }
    
    public boolean create(Branch branch) {
        String sql = "INSERT INTO branches (branch_name, address, phone) VALUES (?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, branch.getBranchName());
            stmt.setString(2, branch.getAddress());
            stmt.setString(3, branch.getPhone());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating branch: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(Branch branch) {
        String sql = "UPDATE branches SET branch_name = ?, address = ?, phone = ? WHERE branch_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, branch.getBranchName());
            stmt.setString(2, branch.getAddress());
            stmt.setString(3, branch.getPhone());
            stmt.setInt(4, branch.getBranchId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating branch: " + e.getMessage());
            return false;
        }
    }
    
    public boolean delete(int branchId) {
        String sql = "DELETE FROM branches WHERE branch_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting branch: " + e.getMessage());
            return false;
        }
    }
    
    private Branch mapResultSetToBranch(ResultSet rs) throws SQLException {
        return new Branch(
            rs.getInt("branch_id"),
            rs.getString("branch_name"),
            rs.getString("address"),
            rs.getString("phone"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
