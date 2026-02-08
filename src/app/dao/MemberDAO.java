package app.dao;

import app.model.Member;
import app.model.Member.Gender;
import app.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

/**
 * MemberDAO - Data Access Object for Member operations
 */
public class MemberDAO {
    
    private final DatabaseUtil dbUtil;
    
    public MemberDAO() {
        this.dbUtil = DatabaseUtil.getInstance();
    }
    
    /**
     * Create new member
     */
    public boolean create(Member member) {
        String sql = "INSERT INTO members (random_id, first_name, last_name, mobile, email, " +
                    "height, weight, gender, date_of_birth, payment, start_date, end_date, " +
                    "period, assigned_branch, assigned_coach, is_active, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, member.getRandomId());
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setString(4, member.getMobile());
            stmt.setString(5, member.getEmail());
            stmt.setBigDecimal(6, member.getHeight());
            stmt.setBigDecimal(7, member.getWeight());
            stmt.setString(8, member.getGender().getDbValue());
            stmt.setDate(9, member.getDateOfBirth() != null ? Date.valueOf(member.getDateOfBirth()) : null);
            stmt.setBigDecimal(10, member.getPayment());
            stmt.setDate(11, Date.valueOf(member.getStartDate()));
            stmt.setDate(12, Date.valueOf(member.getEndDate()));
            stmt.setString(13, member.getPeriod());
            stmt.setInt(14, member.getAssignedBranch());
            
            if (member.getAssignedCoach() != null) {
                stmt.setInt(15, member.getAssignedCoach());
            } else {
                stmt.setNull(15, Types.INTEGER);
            }
            
            stmt.setBoolean(16, member.isActive());
            stmt.setString(17, member.getNotes());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating member: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find member by ID
     */
    public Optional<Member> findById(int memberId) {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding member: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find member by random ID
     */
    public Optional<Member> findByRandomId(int randomId) {
        String sql = "SELECT * FROM members WHERE random_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, randomId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding member by random ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Find members by branch
     */
    public List<Member> findByBranch(int branchId) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE assigned_branch = ? ORDER BY first_name";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding members by branch: " + e.getMessage());
        }
        
        return members;
    }
    
    /**
     * Find members by coach
     */
    public List<Member> findByCoach(int coachId) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE assigned_coach = ? ORDER BY first_name";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, coachId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding members by coach: " + e.getMessage());
        }
        
        return members;
    }
    
    /**
     * Search members by name or mobile
     */
    public List<Member> search(String keyword, Integer branchId) {
        List<Member> members = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM members WHERE (first_name LIKE ? OR last_name LIKE ? OR mobile LIKE ?)"
        );
        
        if (branchId != null) {
            sql.append(" AND assigned_branch = ?");
        }
        
        sql.append(" ORDER BY first_name");
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            if (branchId != null) {
                stmt.setInt(4, branchId);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching members: " + e.getMessage());
        }
        
        return members;
    }
    
    /**
     * Find all members
     */
    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY first_name";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all members: " + e.getMessage());
        }
        
        return members;
    }
    
    /**
     * Update member
     */
    public boolean update(Member member) {
        String sql = "UPDATE members SET random_id = ?, first_name = ?, last_name = ?, " +
                    "mobile = ?, email = ?, height = ?, weight = ?, gender = ?, " +
                    "date_of_birth = ?, payment = ?, start_date = ?, end_date = ?, " +
                    "period = ?, assigned_branch = ?, assigned_coach = ?, is_active = ?, " +
                    "notes = ? WHERE member_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, member.getRandomId());
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setString(4, member.getMobile());
            stmt.setString(5, member.getEmail());
            stmt.setBigDecimal(6, member.getHeight());
            stmt.setBigDecimal(7, member.getWeight());
            stmt.setString(8, member.getGender().getDbValue());
            stmt.setDate(9, member.getDateOfBirth() != null ? Date.valueOf(member.getDateOfBirth()) : null);
            stmt.setBigDecimal(10, member.getPayment());
            stmt.setDate(11, Date.valueOf(member.getStartDate()));
            stmt.setDate(12, Date.valueOf(member.getEndDate()));
            stmt.setString(13, member.getPeriod());
            stmt.setInt(14, member.getAssignedBranch());
            
            if (member.getAssignedCoach() != null) {
                stmt.setInt(15, member.getAssignedCoach());
            } else {
                stmt.setNull(15, Types.INTEGER);
            }
            
            stmt.setBoolean(16, member.isActive());
            stmt.setString(17, member.getNotes());
            stmt.setInt(18, member.getMemberId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete member
     */
    public boolean delete(int memberId) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting member: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get total members count by branch
     */
    public int countByBranch(int branchId) {
        String sql = "SELECT COUNT(*) FROM members WHERE assigned_branch = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting members: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Generate next auto-increment ID
     */
    public int getNextMemberId() {
        String sql = "SELECT MAX(member_id) + 1 FROM members";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int nextId = rs.getInt(1);
                return nextId > 0 ? nextId : 1;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting next member ID: " + e.getMessage());
        }
        
        return 1;
    }
    
    /**
     * Map ResultSet to Member object
     */
    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Integer assignedCoach = rs.getInt("assigned_coach");
        if (rs.wasNull()) assignedCoach = null;
        
        Date dobDate = rs.getDate("date_of_birth");
        LocalDate dateOfBirth = dobDate != null ? dobDate.toLocalDate() : null;
        
        return new Member(
            rs.getInt("member_id"),
            rs.getInt("random_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("mobile"),
            rs.getString("email"),
            rs.getBigDecimal("height"),
            rs.getBigDecimal("weight"),
            Gender.fromString(rs.getString("gender")),
            dateOfBirth,
            rs.getBigDecimal("payment"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getString("period"),
            rs.getInt("assigned_branch"),
            assignedCoach,
            rs.getBoolean("is_active"),
            rs.getString("notes"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
