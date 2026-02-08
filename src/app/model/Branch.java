package app.model;

import java.time.LocalDateTime;

/**
 * Branch Model - Represents a gym branch location
 * Immutable DTO for branch data
 */
public class Branch {
    private final int branchId;
    private final String branchName;
    private final String address;
    private final String phone;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Constructor for new branches (without ID)
    public Branch(String branchName, String address, String phone) {
        this(0, branchName, address, phone, null, null);
    }

    // Full constructor (from database)
    public Branch(int branchId, String branchName, String address, String phone,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getBranchId() { return branchId; }
    public int getId() { return branchId; } // Alias
    public String getBranchName() { return branchName; }
    public String getName() { return branchName; } // Alias
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public boolean isActive() { return true; } // All branches active by default
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Create new branch with updated fields
    public Branch withId(int newId) {
        return new Branch(newId, branchName, address, phone, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return branchName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Branch branch = (Branch) obj;
        return branchId == branch.branchId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(branchId);
    }
}
