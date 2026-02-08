package app.model;

import java.time.LocalDateTime;

/**
 * User Model - Represents system users (Owner, Admin, Coach)
 * Immutable DTO for user data
 */
public class User {
    private final int userId;
    private final String username;
    private final String password; // Hashed password - never store plain text!
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String mobile; // Phone number
    private final UserRole role;
    private final Integer branchId; // Nullable - NULL for owner
    private final boolean isActive;
    private final LocalDateTime lastLogin;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Constructor for new users (without ID)
    public User(String username, String hashedPassword, String firstName, String lastName,
                String email, UserRole role, Integer branchId) {
        this(0, username, hashedPassword, firstName, lastName, email, "", role,
             branchId, true, null, null, null);
    }

    // Full constructor (from database)
    public User(int userId, String username, String password, String firstName, String lastName,
                String email, String mobile, UserRole role, Integer branchId, boolean isActive,
                LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
        this.branchId = branchId;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getUserId() { return userId; }
    public int getId() { return userId; } // Alias for getUserId
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPasswordHash() { return password; } // Alias for getPassword
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public UserRole getRole() { return role; }
    public Integer getBranchId() { return branchId; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Helper methods
    public boolean isOwner() { return role == UserRole.OWNER; }
    public boolean isAdmin() { return role == UserRole.ADMIN; }
    public boolean isCoach() { return role == UserRole.COACH; }

    // Create new user with updated fields
    public User withId(int newId) {
        return new User(newId, username, password, firstName, lastName, email, mobile,
                       role, branchId, isActive, lastLogin, createdAt, updatedAt);
    }

    public User withLastLogin(LocalDateTime newLastLogin) {
        return new User(userId, username, password, firstName, lastName, email, mobile,
                       role, branchId, isActive, newLastLogin, createdAt, updatedAt);
    }

    public User withPassword(String newHashedPassword) {
        return new User(userId, username, newHashedPassword, firstName, lastName, email, mobile,
                       role, branchId, isActive, lastLogin, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getFullName() + " (" + role + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }
}
