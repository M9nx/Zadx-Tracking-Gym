package app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TrainingProgress Model - Represents training progress records
 * Immutable DTO for training progress data
 */
public class TrainingProgress {
    private final int progressId;
    private final int memberId;
    private final int coachId;
    private final LocalDate sessionDate;
    private final String notes;
    private final Integer rating; // 1-5, nullable
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Constructor for new progress records (without ID)
    public TrainingProgress(int memberId, int coachId, LocalDate sessionDate,
                           String notes, Integer rating) {
        this(0, memberId, coachId, sessionDate, notes, rating, null, null);
    }

    // Full constructor (from database)
    public TrainingProgress(int progressId, int memberId, int coachId,
                           LocalDate sessionDate, String notes, Integer rating,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.progressId = progressId;
        this.memberId = memberId;
        this.coachId = coachId;
        this.sessionDate = sessionDate;
        this.notes = notes;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getProgressId() { return progressId; }
    public int getId() { return progressId; } // Alias
    public int getMemberId() { return memberId; }
    public int getCoachId() { return coachId; }
    public LocalDate getSessionDate() { return sessionDate; }
    public String getNotes() { return notes; }
    public Integer getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Create new progress with updated fields
    public TrainingProgress withId(int newId) {
        return new TrainingProgress(newId, memberId, coachId, sessionDate,
                                   notes, rating, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Training Session " + sessionDate + " (Rating: " + 
               (rating != null ? rating + "/5" : "N/A") + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrainingProgress that = (TrainingProgress) obj;
        return progressId == that.progressId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(progressId);
    }
}
