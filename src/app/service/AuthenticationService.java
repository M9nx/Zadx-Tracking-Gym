package app.service;

import app.dao.AuditLogDAO;
import app.dao.UserDAO;
import app.model.User;
import app.model.UserRole;
import app.util.EmailService;
import app.util.PasswordUtil;
import app.util.SessionManager;
import app.util.ValidationUtil;
import java.util.Optional;

/**
 * Authentication Service - Handles user authentication, login, logout, and password management.
 * 
 * This service coordinates the authentication workflow including:
 * - User login with username/password verification
 * - Session creation and management
 * - Password reset for owner via email
 * - Logout and session cleanup
 * - Audit logging for security events
 * 
 * Business Rules:
 * - Passwords must meet complexity requirements
 * - All authentication events are audited
 * - Owner password can only be reset via email to m9nx11@gmail.com
 * - Admins cannot change their own passwords (must request owner)
 * - Inactive users cannot log in
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class AuthenticationService {
    
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;
    private final SessionManager sessionManager;
    private final EmailService emailService;
    
    /**
     * Constructs the AuthenticationService with required dependencies.
     */
    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.sessionManager = SessionManager.getInstance();
        this.emailService = new EmailService();
    }
    
    /**
     * Result object for login attempts containing status and user information.
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        public LoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Optional<User> getUser() {
            return Optional.ofNullable(user);
        }
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * Process:
     * 1. Find user by username
     * 2. Check if user is active
     * 3. Verify password using PasswordUtil
     * 4. Create session via SessionManager
     * 5. Update last login timestamp
     * 6. Log authentication event (success or failure)
     * 
     * @param username the username to authenticate
     * @param password the plain text password to verify
     * @param ipAddress the IP address of the login attempt (for audit logging)
     * @return LoginResult containing success status, message, and user object if successful
     */
    public LoginResult login(String username, String password, String ipAddress) {
        
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            return new LoginResult(false, "Username cannot be empty", null);
        }
        
        if (password == null || password.isEmpty()) {
            return new LoginResult(false, "Password cannot be empty", null);
        }
        
        try {
            // Find user by username
            Optional<User> userOpt = userDAO.findByUsername(username.trim());
            
            if (userOpt.isEmpty()) {
                // Log failed login attempt (unknown username)
                auditLogDAO.create(
                    null, 
                    "LOGIN_FAILED", 
                    "Login attempt with unknown username: " + username, 
                    ipAddress
                );
                return new LoginResult(false, "Invalid username or password", null);
            }
            
            User user = userOpt.get();
            
            // Check if user is active
            if (!user.isActive()) {
                auditLogDAO.create(
                    user.getId(), 
                    "LOGIN_FAILED", 
                    "Login attempt by inactive user: " + username, 
                    ipAddress
                );
                return new LoginResult(false, "Your account has been deactivated. Please contact an administrator.", null);
            }
            
            // Verify password
            boolean passwordValid = PasswordUtil.verifyPassword(password, user.getPasswordHash());
            
            if (!passwordValid) {
                // Log failed login attempt (wrong password)
                auditLogDAO.create(
                    user.getId(), 
                    "LOGIN_FAILED", 
                    "Login attempt with incorrect password for user: " + username, 
                    ipAddress
                );
                return new LoginResult(false, "Invalid username or password", null);
            }
            
            // Login successful - create session
            sessionManager.startSession(user, ipAddress);
            
            // Update last login timestamp
            userDAO.updateLastLogin(user.getId());
            
            // Log successful login
            auditLogDAO.create(
                user.getId(), 
                "LOGIN_SUCCESS", 
                "User logged in successfully: " + username + " (Role: " + user.getRole() + ")", 
                ipAddress
            );
            
            return new LoginResult(true, "Login successful", user);
            
        } catch (Exception e) {
            // Log system error
            auditLogDAO.create(
                null, 
                "LOGIN_ERROR", 
                "System error during login attempt for username: " + username + " - " + e.getMessage(), 
                ipAddress
            );
            
            return new LoginResult(false, "System error during login. Please try again.", null);
        }
    }
    
    /**
     * Logs out the current user and clears the session.
     * 
     * @param ipAddress the IP address of the logout request (for audit logging)
     * @return true if logout was successful, false otherwise
     */
    public boolean logout(String ipAddress) {
        try {
            User currentUser = sessionManager.getCurrentUser();
            
            if (currentUser == null) {
                return false; // No active session
            }
            
            // Log logout event
            auditLogDAO.create(
                currentUser.getId(), 
                "LOGOUT", 
                "User logged out: " + currentUser.getUsername(), 
                ipAddress
            );
            
            // End session
            sessionManager.endSession();
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handles forgot password request for OWNER role only.
     * Generates a new secure password, updates the database, and sends it via email.
     * 
     * Business Rule: Only the owner can reset password via "Forgot Password".
     * The new password is sent to the hard-coded owner email: m9nx11@gmail.com
     * 
     * @param username the username requesting password reset (must be owner)
     * @param ipAddress the IP address of the request (for audit logging)
     * @return true if password reset was successful, false otherwise
     */
    public boolean ownerForgotPassword(String username, String ipAddress) {
        
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Find user by username
            Optional<User> userOpt = userDAO.findByUsername(username.trim());
            
            if (userOpt.isEmpty()) {
                // Log failed attempt (unknown username)
                auditLogDAO.create(
                    null, 
                    "PASSWORD_RESET_FAILED", 
                    "Password reset attempt for unknown username: " + username, 
                    ipAddress
                );
                return false;
            }
            
            User user = userOpt.get();
            
            // Only owner can use forgot password feature
            if (user.getRole() != UserRole.OWNER) {
                auditLogDAO.create(
                    user.getId(), 
                    "PASSWORD_RESET_FAILED", 
                    "Password reset attempt by non-owner user: " + username, 
                    ipAddress
                );
                return false;
            }
            
            // Generate new secure password
            String newPassword = PasswordUtil.generateSecurePassword(12);
            
            // Hash the new password
            String newPasswordHash = PasswordUtil.hashPassword(newPassword);
            
            // Update password in database
            boolean updated = userDAO.updatePassword(user.getId(), newPasswordHash);
            
            if (!updated) {
                auditLogDAO.create(
                    user.getId(), 
                    "PASSWORD_RESET_FAILED", 
                    "Database update failed during password reset for owner: " + username, 
                    ipAddress
                );
                return false;
            }
            
            // Send email with new password
            String ownerEmail = "m9nx11@gmail.com";
            boolean emailSent = emailService.sendOwnerForgotPasswordEmail(
                ownerEmail, 
                username, 
                newPassword
            );
            
            if (!emailSent) {
                // Email failed but password was changed - log warning
                auditLogDAO.create(
                    user.getId(), 
                    "PASSWORD_RESET_WARNING", 
                    "Password reset successful but email delivery failed for owner: " + username, 
                    ipAddress
                );
                
                System.err.println("WARNING: Password was changed but email failed to send!");
                System.err.println("New password for " + username + ": " + newPassword);
                
                return true; // Still return true since password was changed
            }
            
            // Log successful password reset
            auditLogDAO.create(
                user.getId(), 
                "PASSWORD_RESET_SUCCESS", 
                "Owner password reset successfully and email sent: " + username, 
                ipAddress
            );
            
            return true;
            
        } catch (Exception e) {
            auditLogDAO.create(
                null, 
                "PASSWORD_RESET_ERROR", 
                "System error during password reset for username: " + username + " - " + e.getMessage(), 
                ipAddress
            );
            
            System.err.println("Error during password reset: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Changes the password for the current logged-in user.
     * 
     * Business Rules:
     * - User must be logged in
     * - Admins cannot change their own password (must request owner)
     * - Old password must be verified before changing
     * - New password must meet complexity requirements
     * - Owner can change password anytime
     * - Coaches can change their own password
     * 
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @param ipAddress the IP address of the request (for audit logging)
     * @return true if password change was successful, false otherwise
     */
    public boolean changePassword(String oldPassword, String newPassword, String ipAddress) {
        
        // Check if user is logged in
        User currentUser = sessionManager.getCurrentUser();
        
        if (currentUser == null) {
            return false; // No active session
        }
        
        // Admins cannot change their own password
        if (currentUser.getRole() == UserRole.ADMIN) {
            auditLogDAO.create(
                currentUser.getId(), 
                "PASSWORD_CHANGE_FAILED", 
                "Admin attempted to change own password: " + currentUser.getUsername(), 
                ipAddress
            );
            return false;
        }
        
        try {
            // Verify old password
            boolean oldPasswordValid = PasswordUtil.verifyPassword(oldPassword, currentUser.getPasswordHash());
            
            if (!oldPasswordValid) {
                auditLogDAO.create(
                    currentUser.getId(), 
                    "PASSWORD_CHANGE_FAILED", 
                    "Incorrect old password provided by user: " + currentUser.getUsername(), 
                    ipAddress
                );
                return false;
            }
            
            // Validate new password complexity
            ValidationUtil.ValidationResult validationResult = PasswordUtil.validatePasswordComplexity(newPassword);
            
            if (!validationResult.isValid()) {
                auditLogDAO.create(
                    currentUser.getId(), 
                    "PASSWORD_CHANGE_FAILED", 
                    "New password does not meet complexity requirements for user: " + currentUser.getUsername(), 
                    ipAddress
                );
                return false;
            }
            
            // Hash new password
            String newPasswordHash = PasswordUtil.hashPassword(newPassword);
            
            // Update password in database
            boolean updated = userDAO.updatePassword(currentUser.getId(), newPasswordHash);
            
            if (!updated) {
                auditLogDAO.create(
                    currentUser.getId(), 
                    "PASSWORD_CHANGE_FAILED", 
                    "Database update failed during password change for user: " + currentUser.getUsername(), 
                    ipAddress
                );
                return false;
            }
            
            // Log successful password change
            auditLogDAO.create(
                currentUser.getId(), 
                "PASSWORD_CHANGE_SUCCESS", 
                "User changed password successfully: " + currentUser.getUsername(), 
                ipAddress
            );
            
            return true;
            
        } catch (Exception e) {
            auditLogDAO.create(
                currentUser.getId(), 
                "PASSWORD_CHANGE_ERROR", 
                "System error during password change for user: " + currentUser.getUsername() + " - " + e.getMessage(), 
                ipAddress
            );
            
            System.err.println("Error during password change: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if there is an active user session.
     * 
     * @return true if a user is currently logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    /**
     * Gets the currently logged-in user.
     * 
     * @return Optional containing the current user, or empty if not logged in
     */
    public Optional<User> getCurrentUser() {
        User user = sessionManager.getCurrentUser();
        return Optional.ofNullable(user);
    }
}
