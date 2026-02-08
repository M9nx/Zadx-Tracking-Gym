package app.service;

import app.dao.AuditLogDAO;
import app.dao.TrainingProgressDAO;
import app.model.TrainingProgress;
import app.model.User;
import app.util.SessionManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Training Progress Service - Business logic for training progress tracking.
 * 
 * This service handles:
 * - Training session recording
 * - Progress tracking for members
 * - Coach-specific progress queries
 * - Audit logging for training updates
 * 
 * Business Rules:
 * - Only coaches can create training progress records
 * - Coaches can only update progress for their assigned members
 * - Session date cannot be in the future
 * - Rating is optional (1-5 scale if provided)
 * - Notes are required
 * 
 * @author m9nx
 * @version 2.0
 */
public class TrainingProgressService {
    
    private final TrainingProgressDAO progressDAO;
    private final AuditLogDAO auditLogDAO;
    private final SessionManager sessionManager;
    
    /**
     * Constructs the TrainingProgressService with required dependencies.
     */
    public TrainingProgressService() {
        this.progressDAO = new TrainingProgressDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Creates a new training progress record with validation and audit logging.
     * 
     * @param memberId the ID of the member
     * @param coachId the ID of the coach conducting the session
     * @param sessionDate the date of the training session
     * @param notes training notes and observations
     * @param rating optional rating (1-10, null if not rated)
     * @param ipAddress the IP address for audit logging
     * @return the created TrainingProgress object, or empty if creation failed
     */
    public Optional<TrainingProgress> createProgress(
            int memberId,
            int coachId,
            LocalDate sessionDate,
            String notes,
            Integer rating,
            String ipAddress) {
        
        try {
            // Validate inputs
            if (sessionDate == null) {
                System.err.println("Session date is required");
                return Optional.empty();
            }
            
            if (sessionDate.isAfter(LocalDate.now())) {
                System.err.println("Session date cannot be in the future");
                return Optional.empty();
            }
            
            if (notes == null || notes.trim().isEmpty()) {
                System.err.println("Training notes are required");
                return Optional.empty();
            }
            
            // Validate rating (if provided)
            if (rating != null && (rating < 1 || rating > 5)) {
                System.err.println("Rating must be between 1 and 5");
                return Optional.empty();
            }
            
            // Create progress record
            TrainingProgress newProgress = new TrainingProgress(
                memberId,
                coachId,
                sessionDate,
                notes.trim(),
                rating
            );
            
            boolean created = progressDAO.create(newProgress);
            
            if (!created) {
                System.err.println("Failed to create training progress in database");
                return Optional.empty();
            }
            
            // Log training update
            User currentUser = sessionManager.getCurrentUser();
            auditLogDAO.create(
                currentUser != null ? currentUser.getId() : coachId,
                "TRAINING_UPDATE",
                "Recorded training session for member ID: " + memberId + " by coach ID: " + coachId,
                ipAddress
            );
            
            // Return the created progress (note: ID won't be available without re-querying)
            return Optional.of(newProgress);
            
        } catch (Exception e) {
            System.err.println("Error creating training progress: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Updates an existing training progress record.
     * 
     * @param progressId the ID of the progress record to update
     * @param notes the updated training notes
     * @param rating the updated rating (1-10, null if not rated)
     * @param ipAddress the IP address for audit logging
     * @return true if update was successful, false otherwise
     */
    public boolean updateProgress(
            int progressId,
            String notes,
            Integer rating,
            String ipAddress) {
        
        try {
            // Find existing progress
            Optional<TrainingProgress> existingProgressOpt = progressDAO.findById(progressId);
            
            if (existingProgressOpt.isEmpty()) {
                System.err.println("Training progress not found with ID: " + progressId);
                return false;
            }
            
            TrainingProgress existingProgress = existingProgressOpt.get();
            
            // Validate inputs
            if (notes == null || notes.trim().isEmpty()) {
                System.err.println("Training notes are required");
                return false;
            }
            
            // Validate rating (if provided)
            if (rating != null && (rating < 1 || rating > 10)) {
                System.err.println("Rating must be between 1 and 10");
                return false;
            }
            
            // Create updated progress object
            TrainingProgress updatedProgress = new TrainingProgress(
                progressId,
                existingProgress.getMemberId(),
                existingProgress.getCoachId(),
                existingProgress.getSessionDate(),
                notes.trim(),
                rating,
                existingProgress.getCreatedAt(),
                existingProgress.getUpdatedAt()
            );
            
            boolean updated = progressDAO.update(updatedProgress);
            
            if (updated) {
                // Log training update
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "TRAINING_UPDATE",
                    "Updated training session record (ID: " + progressId + ")",
                    ipAddress
                );
            }
            
            return updated;
            
        } catch (Exception e) {
            System.err.println("Error updating training progress: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a training progress record.
     * 
     * @param progressId the ID of the progress record to delete
     * @param ipAddress the IP address for audit logging
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProgress(int progressId, String ipAddress) {
        try {
            Optional<TrainingProgress> progressOpt = progressDAO.findById(progressId);
            
            if (progressOpt.isEmpty()) {
                System.err.println("Training progress not found with ID: " + progressId);
                return false;
            }
            
            boolean deleted = progressDAO.delete(progressId);
            
            if (deleted) {
                // Log training deletion
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "TRAINING_DELETE",
                    "Deleted training session record (ID: " + progressId + ")",
                    ipAddress
                );
            }
            
            return deleted;
            
        } catch (Exception e) {
            System.err.println("Error deleting training progress: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Finds a training progress record by ID.
     */
    public Optional<TrainingProgress> findById(int progressId) {
        return progressDAO.findById(progressId);
    }
    
    /**
     * Gets all training progress records for a specific member.
     */
    public List<TrainingProgress> getProgressByMember(int memberId) {
        return progressDAO.findByMember(memberId);
    }
    
    /**
     * Gets all training progress records created by a specific coach.
     */
    public List<TrainingProgress> getProgressByCoach(int coachId) {
        return progressDAO.findByCoach(coachId);
    }
}
