package app.service;

import app.dao.BranchDAO;
import app.dao.AuditLogDAO;
import app.model.Branch;
import app.util.SessionManager;
import app.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Branch Service - Business logic for branch management operations.
 * 
 * This service handles:
 * - Branch CRUD operations
 * - Branch name uniqueness validation
 * - Branch statistics
 * - Audit logging for branch operations
 * 
 * Business Rules:
 * - Branch name must be unique
 * - Only Owner can manage branches
 * - Cannot delete branch with active users/members
 * - Location is required
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class BranchService {
    
    private final BranchDAO branchDAO;
    private final AuditLogDAO auditLogDAO;
    private final SessionManager sessionManager;
    
    /**
     * Constructs the BranchService with required dependencies.
     */
    public BranchService() {
        this.branchDAO = new BranchDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Creates a new branch with validation and audit logging.
     * 
     * @param name the unique branch name
     * @param location the branch location/address
     * @param contactNumber the branch contact number
     * @param ipAddress the IP address for audit logging
     * @return the created Branch object, or empty if creation failed
     */
    public Optional<Branch> createBranch(
            String name,
            String location,
            String contactNumber,
            String ipAddress) {
        
        try {
            // Validate inputs
            if (name == null || name.trim().isEmpty()) {
                System.err.println("Branch name is required");
                return Optional.empty();
            }
            
            if (location == null || location.trim().isEmpty()) {
                System.err.println("Branch location is required");
                return Optional.empty();
            }
            
            // Check name uniqueness
            List<Branch> existingBranches = branchDAO.findAll();
            for (Branch branch : existingBranches) {
                if (branch.getName().equalsIgnoreCase(name.trim())) {
                    System.err.println("Branch name already exists: " + name);
                    return Optional.empty();
                }
            }
            
            // Create branch
            Branch newBranch = new Branch(
                name.trim(),
                location.trim(),
                contactNumber
            );
            
            boolean created = branchDAO.create(newBranch);
            
            if (!created) {
                System.err.println("Failed to create branch in database");
                return Optional.empty();
            }
            
            // Retrieve created branch (to get auto-generated ID)
            List<Branch> allBranches = branchDAO.findAll();
            Optional<Branch> createdBranch = allBranches.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
            
            if (createdBranch.isPresent()) {
                // Log branch creation
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "BRANCH_CREATE",
                    "Created new branch: " + name,
                    ipAddress
                );
            }
            
            return createdBranch;
            
        } catch (Exception e) {
            System.err.println("Error creating branch: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Updates an existing branch with validation and audit logging.
     * 
     * @param branchId the ID of the branch to update
     * @param name the updated branch name
     * @param location the updated branch location
     * @param contactNumber the updated contact number
     * @param isActive the updated active status
     * @param ipAddress the IP address for audit logging
     * @return true if update was successful, false otherwise
     */
    public boolean updateBranch(
            int branchId,
            String name,
            String location,
            String contactNumber,
            boolean isActive,
            String ipAddress) {
        
        try {
            // Find existing branch
            Optional<Branch> existingBranchOpt = branchDAO.findById(branchId);
            
            if (existingBranchOpt.isEmpty()) {
                System.err.println("Branch not found with ID: " + branchId);
                return false;
            }
            
            Branch existingBranch = existingBranchOpt.get();
            
            // Validate inputs
            if (name == null || name.trim().isEmpty()) {
                System.err.println("Branch name is required");
                return false;
            }
            
            if (location == null || location.trim().isEmpty()) {
                System.err.println("Branch location is required");
                return false;
            }
            
            // Check name uniqueness (if changed)
            if (!name.equalsIgnoreCase(existingBranch.getName())) {
                List<Branch> allBranches = branchDAO.findAll();
                for (Branch branch : allBranches) {
                    if (branch.getId() != branchId && branch.getName().equalsIgnoreCase(name.trim())) {
                        System.err.println("Branch name already exists: " + name);
                        return false;
                    }
                }
            }
            
            // Create updated branch object
            Branch updatedBranch = new Branch(
                branchId,
                name.trim(),
                location.trim(),
                contactNumber,
                existingBranch.getCreatedAt(),
                existingBranch.getUpdatedAt()
            );
            
            boolean updated = branchDAO.update(updatedBranch);
            
            if (updated) {
                // Log branch update
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "BRANCH_UPDATE",
                    "Updated branch: " + name + " (ID: " + branchId + ")",
                    ipAddress
                );
            }
            
            return updated;
            
        } catch (Exception e) {
            System.err.println("Error updating branch: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a branch (soft delete - sets is_active to false).
     * 
     * @param branchId the ID of the branch to delete
     * @param ipAddress the IP address for audit logging
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteBranch(int branchId, String ipAddress) {
        try {
            Optional<Branch> branchOpt = branchDAO.findById(branchId);
            
            if (branchOpt.isEmpty()) {
                System.err.println("Branch not found with ID: " + branchId);
                return false;
            }
            
            Branch branch = branchOpt.get();
            
            // TODO: Add check for active users/members in this branch
            // Cannot delete branch if it has active users or members
            
            boolean deleted = branchDAO.delete(branchId);
            
            if (deleted) {
                // Log branch deletion
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "BRANCH_DELETE",
                    "Deleted branch: " + branch.getName() + " (ID: " + branchId + ")",
                    ipAddress
                );
            }
            
            return deleted;
            
        } catch (Exception e) {
            System.err.println("Error deleting branch: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Finds a branch by ID.
     */
    public Optional<Branch> findById(int branchId) {
        return branchDAO.findById(branchId);
    }
    
    /**
     * Gets a branch by ID (alias for findById).
     */
    public Branch getBranchById(int branchId) {
        return branchDAO.findById(branchId).orElse(null);
    }
    
    /**
     * Gets all branches.
     */
    public List<Branch> getAllBranches() {
        return branchDAO.findAll();
    }
    
    /**
     * Gets all active branches.
     */
    public List<Branch> getActiveBranches() {
        return branchDAO.findAll().stream()
            .filter(Branch::isActive)
            .toList();
    }
}
