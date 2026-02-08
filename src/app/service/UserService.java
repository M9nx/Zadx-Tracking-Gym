package app.service;

import app.dao.UserDAO;
import app.dao.AuditLogDAO;
import app.dao.SystemSettingsDAO;
import app.model.User;
import app.model.UserRole;
import app.util.PasswordUtil;
import app.util.ValidationUtil;
import app.util.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * User Service - Business logic for user management operations.
 * 
 * This service handles:
 * - User CRUD operations
 * - Username and email uniqueness validation
 * - Password complexity enforcement
 * - Role-based filtering
 * - Branch assignment validation
 * - Audit logging for user operations
 * 
 * Business Rules:
 * - Username must be unique
 * - Email must be unique
 * - Passwords must meet complexity requirements
 * - Admins cannot change their own password
 * - Owner has no branch assignment (branch_id = null)
 * - Admins and Coaches must be assigned to a branch
 * - Only Owner can create/modify other Owners
 * 
 * @author m9nx
 * @version 2.0
 */
public class UserService {
    
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;
    private final SessionManager sessionManager;
    
    /**
     * Constructs the UserService with required dependencies.
     */
    public UserService() {
        this.userDAO = new UserDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Creates a new user with validation and audit logging.
     * 
     * Validations:
     * - Username uniqueness
     * - Email uniqueness
     * - Password complexity
     * - Branch assignment rules (Owner = null, Admin/Coach = required)
     * - Role creation permissions
     * 
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param username the unique username
     * @param email the unique email address
     * @param password the plain text password (will be hashed)
     * @param mobile the mobile phone number
     * @param role the user role (OWNER, ADMIN, COACH)
     * @param branchId the branch assignment (null for OWNER, required for ADMIN/COACH)
     * @param ipAddress the IP address for audit logging
     * @return the created User object, or empty if creation failed
     */
    public Optional<User> createUser(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            String mobile,
            UserRole role,
            Integer branchId,
            String ipAddress) {
        
        try {
            // Validate inputs
            ValidationUtil.ValidationResult validation = validateUserInputs(
                firstName, lastName, username, email, password, mobile, role, branchId
            );
            
            if (!validation.isValid()) {
                System.err.println("User creation validation failed: " + validation.getMessage());
                return Optional.empty();
            }
            
            // Check username uniqueness
            if (userDAO.usernameExists(username)) {
                System.err.println("Username already exists: " + username);
                return Optional.empty();
            }
            
            // Check email uniqueness
            if (userDAO.emailExists(email)) {
                System.err.println("Email already exists: " + email);
                return Optional.empty();
            }
            
            // Check if current user has permission to create this role
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null && role == UserRole.OWNER && currentUser.getRole() != UserRole.OWNER) {
                System.err.println("Only Owner can create other Owners");
                return Optional.empty();
            }
            
            // Hash password
            String passwordHash = PasswordUtil.hashPassword(password);
            
            // Create user with full constructor including mobile
            User newUser = new User(
                0, // userId - will be auto-generated
                username,
                passwordHash,
                firstName,
                lastName,
                email,
                mobile != null ? mobile : "",
                role,
                branchId,
                true, // isActive
                null, // lastLogin
                null, // createdAt
                null  // updatedAt
            );
            
            boolean created = userDAO.create(newUser);
            
            if (!created) {
                System.err.println("Failed to create user in database");
                return Optional.empty();
            }
            
            // Retrieve created user (to get auto-generated ID)
            Optional<User> createdUser = userDAO.findByUsername(username);
            
            if (createdUser.isPresent()) {
                // Log user creation
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "USER_CREATE",
                    "Created new user: " + username + " (Role: " + role + ")",
                    ipAddress
                );
                
                // Send welcome email in background thread
                final String finalPassword = password; // Store plain password for email
                new Thread(() -> {
                    try {
                        System.out.println("=== Starting email send process ===");
                        System.out.println("Recipient: " + email);
                        System.out.println("Username: " + username);
                        System.out.println("Role: " + role);
                        sendWelcomeEmail(firstName, lastName, email, username, finalPassword, role.toString());
                        System.out.println("=== Email send completed successfully ===");
                    } catch (Exception e) {
                        System.err.println("=== FAILED to send welcome email ===");
                        System.err.println("Error: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Show error to user
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            String errorMsg;
                            if (e.getMessage() != null && e.getMessage().contains("UnknownHostException")) {
                                errorMsg = "Network Error: Cannot connect to email server.\n\n" +
                                          "Possible causes:\n" +
                                          "• No internet connection\n" +
                                          "• Firewall blocking SMTP (port 465)\n" +
                                          "• Antivirus blocking Java\n\n" +
                                          "User created successfully, but welcome email could not be sent.";
                            } else if (e.getMessage() != null && e.getMessage().contains("Authentication")) {
                                errorMsg = "Email Authentication Failed\n\n" +
                                          "Please check email settings in System Settings.\n\n" +
                                          "User created successfully, but welcome email could not be sent.";
                            } else {
                                errorMsg = "Failed to send welcome email:\n" + e.getMessage() + "\n\n" +
                                          "User created successfully, but welcome email could not be sent.";
                            }
                            javax.swing.JOptionPane.showMessageDialog(null, 
                                errorMsg, 
                                "Email Sending Error", 
                                javax.swing.JOptionPane.WARNING_MESSAGE);
                        });
                    }
                }).start();
            }
            
            return createdUser;
            
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Updates an existing user with validation and audit logging.
     * 
     * Note: Password updates are handled by updatePassword() method.
     * 
     * @param userId the ID of the user to update
     * @param firstName the updated first name
     * @param lastName the updated last name
     * @param email the updated email address
     * @param mobile the updated mobile phone number
     * @param role the updated user role
     * @param branchId the updated branch assignment
     * @param isActive the updated active status
     * @param ipAddress the IP address for audit logging
     * @return true if update was successful, false otherwise
     */
    public boolean updateUser(
            int userId,
            String firstName,
            String lastName,
            String email,
            String mobile,
            UserRole role,
            Integer branchId,
            boolean isActive,
            String ipAddress) {
        
        try {
            // Find existing user
            Optional<User> existingUserOpt = userDAO.findById(userId);
            
            if (existingUserOpt.isEmpty()) {
                System.err.println("User not found with ID: " + userId);
                return false;
            }
            
            User existingUser = existingUserOpt.get();
            
            // Validate inputs (no password validation on update)
            if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                mobile == null || mobile.trim().isEmpty()) {
                System.err.println("Required fields cannot be empty");
                return false;
            }
            
            // Validate email format
            if (!ValidationUtil.validateEmail(email).isValid()) {
                System.err.println("Invalid email format");
                return false;
            }
            
            // Validate mobile format
            if (!ValidationUtil.validateMobile(mobile).isValid()) {
                System.err.println("Invalid mobile format");
                return false;
            }
            
            // Validate branch assignment rules
            if (role == UserRole.OWNER && branchId != null) {
                System.err.println("Owner cannot be assigned to a branch");
                return false;
            }
            
            if ((role == UserRole.ADMIN || role == UserRole.COACH) && branchId == null) {
                System.err.println("Admin/Coach must be assigned to a branch");
                return false;
            }
            
            // Check email uniqueness (if changed)
            if (!email.equalsIgnoreCase(existingUser.getEmail()) && userDAO.emailExists(email)) {
                System.err.println("Email already exists: " + email);
                return false;
            }
            
            // Create updated user object
            User updatedUser = new User(
                userId,
                existingUser.getUsername(), // Username cannot be changed
                existingUser.getPassword(), // Password not changed here
                firstName,
                lastName,
                email,
                mobile,
                role,
                branchId,
                isActive,
                existingUser.getLastLogin(),
                existingUser.getCreatedAt(),
                existingUser.getUpdatedAt()
            );
            
            boolean updated = userDAO.update(updatedUser);
            
            if (updated) {
                // Log user update
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "USER_UPDATE",
                    "Updated user: " + existingUser.getUsername() + " (ID: " + userId + ")",
                    ipAddress
                );
            }
            
            return updated;
            
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a user (soft delete - sets is_active to false).
     * 
     * @param userId the ID of the user to delete
     * @param ipAddress the IP address for audit logging
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteUser(int userId, String ipAddress) {
        try {
            Optional<User> userOpt = userDAO.findById(userId);
            
            if (userOpt.isEmpty()) {
                System.err.println("User not found with ID: " + userId);
                return false;
            }
            
            User user = userOpt.get();
            
            // Prevent deleting self
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getId() == userId) {
                System.err.println("Cannot delete your own account");
                return false;
            }
            
            boolean deleted = userDAO.delete(userId);
            
            if (deleted) {
                // Log user deletion
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "USER_DELETE",
                    "Deleted user: " + user.getUsername() + " (ID: " + userId + ")",
                    ipAddress
                );
            }
            
            return deleted;
            
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(int userId) {
        return userDAO.findById(userId);
    }
    
    /**
     * Finds a user by username.
     */
    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }
    
    /**
     * Gets all users with a specific role.
     */
    public List<User> getUsersByRole(UserRole role) {
        return userDAO.findByRole(role);
    }
    
    /**
     * Gets all users assigned to a specific branch.
     */
    public List<User> getUsersByBranch(int branchId) {
        return userDAO.findByBranch(branchId);
    }
    
    /**
     * Gets all coaches assigned to a specific branch.
     */
    public List<User> getCoachesByBranch(int branchId) {
        return userDAO.findCoachesByBranch(branchId);
    }
    
    /**
     * Checks if a username already exists.
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }
    
    /**
     * Checks if an email already exists.
     */
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }
    
    /**
     * Validates user input fields.
     */
    private ValidationUtil.ValidationResult validateUserInputs(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            String mobile,
            UserRole role,
            Integer branchId) {
        
        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Last name is required");
        }
        
        if (username == null || username.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Username is required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Email is required");
        }
        
        if (password == null || password.isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Password is required");
        }
        
        if (mobile == null || mobile.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Mobile is required");
        }
        
        if (role == null) {
            return new ValidationUtil.ValidationResult(false, "Role is required");
        }
        
        // Validate username format
        ValidationUtil.ValidationResult usernameValidation = ValidationUtil.validateUsername(username);
        if (!usernameValidation.isValid()) {
            return usernameValidation;
        }
        
        // Validate email format
        ValidationUtil.ValidationResult emailValidation = ValidationUtil.validateEmail(email);
        if (!emailValidation.isValid()) {
            return emailValidation;
        }
        
        // Validate password complexity
        ValidationUtil.ValidationResult passwordValidation = PasswordUtil.validatePasswordComplexity(password);
        if (!passwordValidation.isValid()) {
            return passwordValidation;
        }
        
        // Validate mobile format
        ValidationUtil.ValidationResult mobileValidation = ValidationUtil.validateMobile(mobile);
        if (!mobileValidation.isValid()) {
            return mobileValidation;
        }
        
        // Validate branch assignment rules
        if (role == UserRole.OWNER && branchId != null) {
            return new ValidationUtil.ValidationResult(false, "Owner cannot be assigned to a branch");
        }
        
        if ((role == UserRole.ADMIN || role == UserRole.COACH) && branchId == null) {
            return new ValidationUtil.ValidationResult(false, "Admin/Coach must be assigned to a branch");
        }
        
        return new ValidationUtil.ValidationResult(true, "Valid");
    }
    
    /**
     * Gets all users by branch and role.
     */
    public List<User> getUsersByBranchAndRole(int branchId, UserRole role) {
        return userDAO.findAll().stream()
            .filter(user -> user.getBranchId() != null && user.getBranchId() == branchId)
            .filter(user -> user.getRole() == role)
            .toList();
    }
    
    /**
     * Sends a welcome email to a newly created admin user.
     */
    private void sendWelcomeEmail(String firstName, String lastName, String recipientEmail, String username, String password, String role) throws Exception {
            System.out.println(">>> Fetching email settings from database...");
            SystemSettingsDAO settingsDAO = new SystemSettingsDAO();
            
            String senderEmail = settingsDAO.get("email.sender_address");
            String smtpHost = settingsDAO.get("email.smtp_host");
            String smtpPort = settingsDAO.get("email.smtp_port");
            String smtpUsername = settingsDAO.get("email.smtp_username");
            String smtpPassword = settingsDAO.get("email.smtp_password");
            String smtpTLS = settingsDAO.get("email.smtp_tls");
            
            System.out.println(">>> Email settings retrieved:");
            System.out.println("    Sender: " + senderEmail);
            System.out.println("    SMTP Host: " + smtpHost);
            System.out.println("    SMTP Port: " + smtpPort);
            System.out.println("    SMTP Username: " + smtpUsername);
            System.out.println("    SMTP Password: " + (smtpPassword != null ? "[SET - " + smtpPassword.length() + " chars]" : "[NOT SET]"));
            System.out.println("    TLS Enabled: " + smtpTLS);
            
            // Use defaults if not configured
            if (senderEmail == null) senderEmail = "gym.zadx@gmail.com";
            if (smtpHost == null) smtpHost = "smtp.gmail.com";
            if (smtpPort == null) smtpPort = "587";
            if (smtpUsername == null) smtpUsername = "gym.zadx@gmail.com";
            if (smtpPassword == null || smtpPassword.isEmpty()) {
                System.err.println("❌ ERROR: Email password not configured in database!");
                System.err.println("❌ Please run: sql/fix_email_and_regist.sql");
                return;
            }
            if (smtpTLS == null) smtpTLS = "true";
            
            // Create final variables for use in anonymous class
            final String finalUsername = smtpUsername;
            final String finalPassword = smtpPassword;
            
            // SMTP configuration
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            
            // Force IPv4 (fixes DNS resolution issues)
            System.setProperty("java.net.preferIPv4Stack", "true");
            
            // SSL configuration for port 465
            if ("465".equals(smtpPort)) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            } else {
                // TLS configuration for port 587
                props.put("mail.smtp.starttls.enable", smtpTLS);
            }
            
            // Connection timeouts
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
            
            Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(finalUsername, finalPassword);
                    }
                });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail, "Gym Zadx Management"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Welcome to Gym Zadx - Admin Account Created");
            
            String htmlContent = "<!DOCTYPE html>"
                + "<html><head><style>"
                + "body { font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 20px; }"
                + ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.3); }"
                + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 20px; text-align: center; }"
                + ".header h1 { margin: 0; font-size: 32px; font-weight: bold; }"
                + ".header p { margin: 10px 0 0 0; font-size: 18px; opacity: 0.9; }"
                + ".content { padding: 40px 30px; }"
                + ".welcome { font-size: 24px; color: #333; margin-bottom: 20px; font-weight: 600; }"
                + ".message { font-size: 16px; color: #555; line-height: 1.8; margin-bottom: 30px; }"
                + ".info-box { background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 10px; padding: 25px; margin: 25px 0; border-left: 4px solid #667eea; }"
                + ".info-box h3 { margin: 0 0 15px 0; color: #667eea; font-size: 18px; }"
                + ".info-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid rgba(0,0,0,0.1); }"
                + ".info-item:last-child { border-bottom: none; }"
                + ".info-label { font-weight: 600; color: #444; }"
                + ".info-value { color: #667eea; font-weight: 600; }"
                + ".features { margin: 30px 0; }"
                + ".feature-item { padding: 15px; margin: 10px 0; background: #f8f9fa; border-radius: 8px; border-left: 3px solid #667eea; }"
                + ".feature-item strong { color: #667eea; display: block; margin-bottom: 5px; }"
                + ".footer { background: #f8f9fa; padding: 30px; text-align: center; color: #777; font-size: 14px; border-top: 1px solid #e0e0e0; }"
                + ".footer strong { color: #667eea; display: block; margin-bottom: 10px; font-size: 16px; }"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>🏋️ Gym Zadx</h1>"
                + "<p>Management System</p>"
                + "</div>"
                + "<div class='content'>"
                + "<div class='welcome'>Welcome to the Team, " + firstName + "! 🎉</div>"
                + "<div class='message'>"
                + "Congratulations! You have been successfully registered as an <strong>" + role + "</strong> at Gym Zadx. "
                + "You now have access to our comprehensive gym management system with powerful tools to manage members, coaches, and operations."
                + "</div>"
                + "<div class='info-box'>"
                + "<h3>👤 Your Account Details</h3>"
                + "<div class='info-item'><span class='info-label'>Full Name:</span><span class='info-value'>" + firstName + " " + lastName + "</span></div>"
                + "<div class='info-item'><span class='info-label'>Username:</span><span class='info-value'>" + username + "</span></div>"
                + "<div class='info-item'><span class='info-label'>Password:</span><span class='info-value'>" + password + "</span></div>"
                + "<div class='info-item'><span class='info-label'>Email:</span><span class='info-value'>" + recipientEmail + "</span></div>"
                + "<div class='info-item'><span class='info-label'>Role:</span><span class='info-value'>" + role + "</span></div>"
                + "</div>"
                + "<div class='features'>"
                + "<h3 style='color: #333; margin-bottom: 15px;'>✨ What You Can Do:</h3>"
                + "<div class='feature-item'><strong>Member Management</strong>Add, edit, and track gym members with ease</div>"
                + "<div class='feature-item'><strong>Coach Management</strong>Manage coach profiles and assignments</div>"
                + "<div class='feature-item'><strong>Financial Reports</strong>View detailed statistics and revenue analytics</div>"
                + "<div class='feature-item'><strong>System Configuration</strong>Configure system-wide settings and preferences</div>"
                + "</div>"
                + "<div class='message' style='margin-top: 30px;'>"
                + "📌 <strong>Important:</strong> Please keep your login credentials secure. You can change your password anytime from the system settings."
                + "</div>"
                + "</div>"
                + "<div class='footer'>"
                + "<strong>Gym Zadx Management System</strong>"
                + "<p>This is an automated message. Please do not reply to this email.</p>"
                + "<p style='margin-top: 15px; color: #999;'>© 2025 Gym Zadx. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body></html>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            System.out.println(">>> Sending email via SMTP...");
            Transport.send(message);
            
            System.out.println("✅ SUCCESS: Welcome email sent to: " + recipientEmail);
    }
    
    /**
     * Reset password by username and email verification.
     * Used by PasswordResetPage for email-based password reset.
     * 
     * @param username the username to reset password for
     * @param email the email address to verify ownership
     * @param ipAddress the IP address for audit logging
     * @return "SUCCESS:newPassword" if successful, "ERROR:message" if failed
     */
    public String resetPasswordByEmail(String username, String email, String ipAddress) {
        try {
            // Find user by username
            Optional<User> userOpt = userDAO.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return "ERROR:Username not found. Please check your username and try again.";
            }
            
            User user = userOpt.get();
            
            // Verify email matches
            if (!email.equalsIgnoreCase(user.getEmail())) {
                return "ERROR:Email address does not match our records. Please check and try again.";
            }
            
            // Check if user is active
            if (!user.isActive()) {
                return "ERROR:This account is inactive. Please contact the system administrator.";
            }
            
            // Generate new random password
            String newPassword = PasswordUtil.generateSecurePassword(10);
            
            // Hash the new password
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            // Update password in database
            boolean updated = userDAO.updatePassword(user.getId(), hashedPassword);
            
            if (!updated) {
                return "ERROR:Failed to update password. Please try again later.";
            }
            
            // Send email with new password
            try {
                sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getUsername(),
                    newPassword,
                    user.getRole().name()
                );
            } catch (Exception emailEx) {
                System.err.println("Warning: Password updated but email failed: " + emailEx.getMessage());
                // Don't return error - password was updated successfully
            }
            
            // Log the password reset
            auditLogDAO.create(
                user.getId(),
                "PASSWORD_RESET_BY_EMAIL",
                "Password reset via email verification. New password generated and emailed to user.",
                ipAddress
            );
            
            System.out.println("✅ Password reset successful for user: " + username);
            return "SUCCESS:" + newPassword;
            
        } catch (Exception e) {
            System.err.println("Error in resetPasswordByEmail: " + e.getMessage());
            e.printStackTrace();
            return "ERROR:An unexpected error occurred. Please try again later.";
        }
    }
    
    /**
     * Send password reset email with new credentials.
     */
    private void sendPasswordReseEmail(String recipientEmail, String fullName, 
            String username, Stritng newPassword, String role) throws Exception {
        
        // Get SMTP settings
        SystemSettingsDAO settingsDAO = new SystemSettingsDAO();
        String smtpHostTemp = settingsDAO.get("smtp_host");
        final String smtpHost = (smtpHostTemp != null) ? smtpHostTemp : "smtp.gmail.com";
        String smtpPortTemp = settingsDAO.get("smtp_port");
        final String smtpPort = (smtpPortTemp != null) ? smtpPortTemp : "587";
        String smtpUserTemp = settingsDAO.get("smtp_username");
        final String smtpUser = (smtpUserTemp != null) ? smtpUserTemp : "";
        String smtpPasswordTemp = settingsDAO.get("smtp_password");
        final String smtpPassword = (smtpPasswordTemp != null) ? smtpPasswordTemp : "";
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpUser, "Gym Zadx Management System"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Password Reset - Gym Zadx Management System");
        
        String htmlContent = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
            + "body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }"
            + ".container { max-width: 600px; margin: 30px auto; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }"
            + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 20px; text-align: center; }"
            + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; }"
            + ".header p { margin: 5px 0 0 0; font-size: 14px; opacity: 0.95; }"
            + ".content { padding: 40px 30px; }"
            + ".alert { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px; }"
            + ".alert h3 { margin: 0 0 10px 0; color: #856404; font-size: 18px; }"
            + ".info-box { background: #f8f9fa; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; margin: 20px 0; }"
            + ".info-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #e0e0e0; }"
            + ".info-item:last-child { border-bottom: none; }"
            + ".info-label { font-weight: 600; color: #555; }"
            + ".info-value { color: #333; font-weight: 500; }"
            + ".password-box { background: #667eea; color: white; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0; }"
            + ".password-box h3 { margin: 0 0 10px 0; font-size: 16px; }"
            + ".password-box .password { font-size: 24px; font-weight: 700; letter-spacing: 2px; font-family: 'Courier New', monospace; }"
            + ".footer { background: #f8f9fa; padding: 30px; text-align: center; color: #777; font-size: 14px; border-top: 1px solid #e0e0e0; }"
            + ".footer strong { color: #667eea; display: block; margin-bottom: 10px; font-size: 16px; }"
            + "</style></head><body>"
            + "<div class='container'>"
            + "<div class='header'>"
            + "<h1>🔐 Password Reset</h1>"
            + "<p>Gym Zadx Management System</p>"
            + "</div>"
            + "<div class='content'>"
            + "<h2 style='color: #333; margin-bottom: 20px;'>Hello, " + fullName + "!</h2>"
            + "<p style='font-size: 16px; color: #555;'>"
            + "Your password has been successfully reset as requested. Below are your new login credentials."
            + "</p>"
            + "<div class='alert'>"
            + "<h3>⚠️ Important Security Notice</h3>"
            + "<p style='margin: 0; color: #856404;'>"
            + "For security reasons, we recommend changing this password immediately after logging in. "
            + "Never share your password with anyone."
            + "</p>"
            + "</div>"
            + "<div class='info-box'>"
            + "<h3 style='margin: 0 0 15px 0; color: #333;'>👤 Your Login Credentials</h3>"
            + "<div class='info-item'><span class='info-label'>Full Name:</span><span class='info-value'>" + fullName + "</span></div>"
            + "<div class='info-item'><span class='info-label'>Username:</span><span class='info-value'>" + username + "</span></div>"
            + "<div class='info-item'><span class='info-label'>Role:</span><span class='info-value'>" + role + "</span></div>"
            + "</div>"
            + "<div class='password-box'>"
            + "<h3>🔑 Your New Password</h3>"
            + "<div class='password'>" + newPassword + "</div>"
            + "</div>"
            + "<p style='font-size: 14px; color: #666; margin-top: 30px;'>"
            + "📌 <strong>Next Steps:</strong><br>"
            + "1. Use your username and new password to login<br>"
            + "2. Change your password from your profile settings<br>"
            + "3. Choose a strong, unique password<br>"
            + "4. Keep your credentials secure"
            + "</p>"
            + "<p style='font-size: 14px; color: #999; margin-top: 20px;'>"
            + "If you did not request this password reset, please contact the system administrator immediately."
            + "</p>"
            + "</div>"
            + "<div class='footer'>"
            + "<strong>Gym Zadx Management System</strong>"
            + "<p>This is an automated message. Please do not reply to this email.</p>"
            + "<p style='margin-top: 15px; color: #999;'>© 2025 Gym Zadx. All rights reserved.</p>"
            + "</div>"
            + "</div>"
            + "</body></html>";
        
        message.setContent(htmlContent, "text/html; charset=utf-8");
        
        System.out.println(">>> Sending password reset email via SMTP...");
        Transport.send(message);
        
        System.out.println("✅ SUCCESS: Password reset email sent to: " + recipientEmail);
    }
}
