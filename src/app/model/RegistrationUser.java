package app.model;

import java.time.LocalDateTime;

/**
 * RegistrationUser Model - Represents legacy user registration data
 * Maps to: regist table (DEPRECATED - for backward compatibility only)
 * 
 * ⚠️ DEPRECATION NOTICE:
 * This table supports the old LOGIN.java interface.
 * For new development, use the 'users' table and User.java model.
 * 
 * This model is maintained for backward compatibility with existing code
 * that still uses the legacy authentication system.
 * 
 * Immutable DTO for legacy registration data
 */
public class RegistrationUser {
    private final int id;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password; // Should be hashed, not plain text!
    private final UserRole role;
    private final String securityQuestion;
    private final String securityAnswer; // Should be hashed
    private final LocalDateTime createdAt;

    /**
     * Constructor for new registration (without ID)
     * @param firstName User's first name
     * @param lastName User's last name
     * @param username Unique username
     * @param hashedPassword Hashed password (NEVER plain text!)
     * @param role User role (owner, admin, coach)
     * @param securityQuestion Security question for password recovery
     * @param securityAnswer Hashed security answer
     */
    public RegistrationUser(String firstName, String lastName, String username,
                           String hashedPassword, UserRole role,
                           String securityQuestion, String securityAnswer) {
        this(0, firstName, lastName, username, hashedPassword, role,
             securityQuestion, securityAnswer, null);
    }

    /**
     * Full constructor (from database)
     * @param id Primary key
     * @param firstName User's first name
     * @param lastName User's last name
     * @param username Unique username
     * @param password Hashed password
     * @param role User role
     * @param securityQuestion Security question
     * @param securityAnswer Hashed security answer
     * @param createdAt Creation timestamp
     */
    public RegistrationUser(int id, String firstName, String lastName,
                           String username, String password, UserRole role,
                           String securityQuestion, String securityAnswer,
                           LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role != null ? role : UserRole.ADMIN; // Default to admin if null
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { 
        return id; 
    }

    public String getFirstName() { 
        return firstName; 
    }

    public String getLastName() { 
        return lastName; 
    }

    public String getFullName() { 
        return firstName + " " + lastName; 
    }

    public String getUsername() { 
        return username; 
    }

    public String getPassword() { 
        return password; 
    }

    public String getPasswordHash() { 
        return password; 
    }

    public UserRole getRole() { 
        return role; 
    }

    public String getSecurityQuestion() { 
        return securityQuestion; 
    }

    public String getSecurityAnswer() { 
        return securityAnswer; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    // Helper methods
    public boolean isOwner() { 
        return role == UserRole.OWNER; 
    }

    public boolean isAdmin() { 
        return role == UserRole.ADMIN; 
    }

    public boolean isCoach() { 
        return role == UserRole.COACH; 
    }

    /**
     * Convert legacy registration user to modern User model
     * Note: Branch ID will be null, needs to be set separately
     * @return User instance
     */
    public User toUser() {
        return new User(id, username, password, firstName, lastName,
                       "", "", role, null, true, null, createdAt, null);
    }

    // Immutable update methods

    /**
     * Create new instance with updated password
     * @param newHashedPassword New hashed password
     * @return New RegistrationUser instance
     */
    public RegistrationUser withPassword(String newHashedPassword) {
        return new RegistrationUser(id, firstName, lastName, username,
                                   newHashedPassword, role, securityQuestion,
                                   securityAnswer, createdAt);
    }

    /**
     * Create new instance with updated security answer
     * @param newSecurityAnswer New hashed security answer
     * @return New RegistrationUser instance
     */
    public RegistrationUser withSecurityAnswer(String newSecurityAnswer) {
        return new RegistrationUser(id, firstName, lastName, username,
                                   password, role, securityQuestion,
                                   newSecurityAnswer, createdAt);
    }

    /**
     * Create new instance with ID (after database insert)
     * @param newId Database-generated ID
     * @return New RegistrationUser instance
     */
    public RegistrationUser withId(int newId) {
        return new RegistrationUser(newId, firstName, lastName, username,
                                   password, role, securityQuestion,
                                   securityAnswer, createdAt);
    }

    /**
     * Create new instance with role
     * @param newRole New user role
     * @return New RegistrationUser instance
     */
    public RegistrationUser withRole(UserRole newRole) {
        return new RegistrationUser(id, firstName, lastName, username,
                                   password, newRole, securityQuestion,
                                   securityAnswer, createdAt);
    }

    @Override
    public String toString() {
        return getFullName() + " (" + username + ", " + role + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RegistrationUser that = (RegistrationUser) obj;
        return id == that.id || (username != null && username.equals(that.username));
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : id;
    }
}
