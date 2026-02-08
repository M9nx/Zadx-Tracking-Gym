package app.views.admin;

import app.dao.BranchDAO;
import app.dao.UserDAO;
import app.model.Branch;
import app.model.User;
import app.util.EmailService;
import java.awt.*;
import java.security.SecureRandom;
import javax.swing.*;

/**
 * AdminProfileView - Admin profile page with read-only settings
 * Features:
 * - Display current name, email, branch
 * - Password reset (sends new password to email)
 * - Request branch transfer (alerts owner)
 * - Cannot edit system settings
 */
public class AdminProfileView extends JPanel {
    
    private final User currentUser;
    private final UserDAO userDAO;
    private final BranchDAO branchDAO;
    private final EmailService emailService;
    
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField usernameField;
    private JTextField branchField;
    private JTextField roleField;
    
    public AdminProfileView(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.branchDAO = new BranchDAO();
        this.emailService = new EmailService();
        
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        initComponents();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Semi-transparent blur background
        g2d.setColor(new Color(50, 50, 50, 160));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2d.dispose();
    }
    
    private void initComponents() {
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Main Content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setOpaque(false);
        
        // Profile Info Section
        JPanel profileSection = createProfileSection();
        contentPanel.add(profileSection, BorderLayout.NORTH);
        
        // Actions Section
        JPanel actionsSection = createActionsSection();
        contentPanel.add(actionsSection, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("👤 Admin Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(220, 220, 220));
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createProfileSection() {
        JPanel panel = createSectionPanel("Profile Information");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        
        int row = 0;
        
        // First Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(createLabel("First Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        firstNameField = createReadOnlyField(currentUser.getFirstName());
        panel.add(firstNameField, gbc);
        row++;
        
        // Last Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(createLabel("Last Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        lastNameField = createReadOnlyField(currentUser.getLastName());
        panel.add(lastNameField, gbc);
        row++;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(createLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = createReadOnlyField(currentUser.getUsername());
        panel.add(usernameField, gbc);
        row++;
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = createReadOnlyField(currentUser.getEmail());
        panel.add(emailField, gbc);
        row++;
        
        // Role
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(createLabel("Role:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        roleField = createReadOnlyField(currentUser.getRole().toString());
        panel.add(roleField, gbc);
        row++;
        
        // Branch
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(createLabel("Current Branch:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String branchName = getBranchName(currentUser.getBranchId());
        branchField = createReadOnlyField(branchName);
        panel.add(branchField, gbc);
        
        return panel;
    }
    
    private JPanel createActionsSection() {
        JPanel panel = createSectionPanel("Account Actions");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Reset Password Button
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        JButton resetPasswordBtn = createActionButton("Reset My Password");
        resetPasswordBtn.addActionListener(e -> resetPassword());
        panel.add(resetPasswordBtn, gbc);
        row++;
        
        // Help text for reset password
        gbc.gridy = row;
        JLabel resetHelpLabel = new JLabel("A new password will be generated and sent to your email");
        resetHelpLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        resetHelpLabel.setForeground(new Color(180, 180, 190));
        resetHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(resetHelpLabel, gbc);
        row++;
        
        // Spacer
        gbc.gridy = row;
        gbc.insets = new Insets(20, 15, 10, 15);
        panel.add(Box.createVerticalStrut(20), gbc);
        row++;
        
        // Request Branch Transfer Button
        gbc.gridy = row;
        gbc.insets = new Insets(10, 15, 10, 15);
        JButton requestTransferBtn = createActionButton("Request Branch Transfer");
        requestTransferBtn.addActionListener(e -> requestBranchTransfer());
        panel.add(requestTransferBtn, gbc);
        row++;
        
        // Help text for transfer
        gbc.gridy = row;
        JLabel transferHelpLabel = new JLabel("Request to be transferred to another branch (Owner will be notified)");
        transferHelpLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        transferHelpLabel.setForeground(new Color(180, 180, 190));
        transferHelpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(transferHelpLabel, gbc);
        row++;
        
        // Spacer
        gbc.gridy = row;
        gbc.insets = new Insets(20, 15, 10, 15);
        panel.add(Box.createVerticalStrut(20), gbc);
        row++;
        
        // Info about settings
        gbc.gridy = row;
        gbc.insets = new Insets(10, 15, 10, 15);
        JPanel infoPanel = createInfoPanel();
        panel.add(infoPanel, gbc);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 110), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel iconLabel = new JLabel("ℹ");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(new Color(139, 92, 246));
        panel.add(iconLabel, BorderLayout.WEST);
        
        JLabel textLabel = new JLabel("<html><b>Note:</b> As an Admin, you cannot access System Settings.<br>Contact the Owner for any system configuration changes.</html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(new Color(220, 220, 230));
        panel.add(textLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void resetPassword() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset your password?\nA new password will be generated and sent to: " + currentUser.getEmail(),
            "Confirm Password Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Generate new password
            String newPassword = generateRandomPassword(12);
            
            // Hash the new password
            String hashedPassword = app.util.PasswordUtil.hashPassword(newPassword);
            
            // Update password in database
            boolean updated = userDAO.updatePassword(currentUser.getId(), hashedPassword);
            
            if (updated) {
                // Send email with new password
                sendPasswordResetEmail(currentUser.getEmail(), currentUser.getFirstName(), newPassword);
                
                JOptionPane.showMessageDialog(
                    this,
                    "Password reset successfully!\nYour new password has been sent to: " + currentUser.getEmail(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to reset password. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error resetting password: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    private void requestBranchTransfer() {
        // Get all branches for selection
        java.util.List<Branch> branches = branchDAO.findAll();
        
        if (branches.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No branches available.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Create branch selection dialog
        String[] branchNames = branches.stream()
            .map(b -> b.getName() + " (" + b.getAddress() + ")")
            .toArray(String[]::new);
        
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select the branch you want to transfer to:",
            "Request Branch Transfer",
            JOptionPane.QUESTION_MESSAGE,
            null,
            branchNames,
            branchNames[0]
        );
        
        if (selected == null) {
            return; // User cancelled
        }
        
        // Find selected branch
        int selectedIndex = java.util.Arrays.asList(branchNames).indexOf(selected);
        Branch targetBranch = branches.get(selectedIndex);
        
        if (currentUser.getBranchId() != null && targetBranch.getBranchId() == currentUser.getBranchId()) {
            JOptionPane.showMessageDialog(
                this,
                "You are already assigned to this branch.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        // Ask for reason
        String reason = JOptionPane.showInputDialog(
            this,
            "Please provide a reason for the transfer request:",
            "Transfer Reason",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (reason == null || reason.trim().isEmpty()) {
            return; // User cancelled
        }
        
        try {
            // Send notification to owner
            sendTransferRequestToOwner(targetBranch, reason.trim());
            
            JOptionPane.showMessageDialog(
                this,
                "Transfer request submitted successfully!\nThe Owner will be notified via email.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error submitting transfer request: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    private void sendPasswordResetEmail(String email, String firstName, String newPassword) {
        new Thread(() -> {
            try {
                String subject = "Password Reset Successful - Gym Management System";
                String htmlContent = buildPasswordResetEmailTemplate(firstName, newPassword);
                
                boolean emailSent = emailService.sendEmail(email, subject, htmlContent, true);
                if (emailSent) {
                    System.out.println("Password reset email sent successfully to: " + email);
                } else {
                    System.err.println("Failed to send password reset email to: " + email);
                }
                
            } catch (Exception e) {
                System.err.println("Failed to send password reset email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void sendTransferRequestToOwner(Branch targetBranch, String reason) {
        new Thread(() -> {
            try {
                // Find owner email from system settings or hardcoded
                String ownerEmail = getOwnerEmail();
                
                if (ownerEmail == null || ownerEmail.isEmpty()) {
                    System.err.println("Owner email not configured");
                    return;
                }
                
                String subject = "Branch Transfer Request - " + currentUser.getUsername();
                String htmlContent = buildTransferRequestEmailTemplate(targetBranch, reason);
                
                boolean emailSent = emailService.sendEmail(ownerEmail, subject, htmlContent, true);
                if (emailSent) {
                    System.out.println("Transfer request email sent to owner: " + ownerEmail);
                } else {
                    System.err.println("Failed to send transfer request email to: " + ownerEmail);
                }
                
            } catch (Exception e) {
                System.err.println("Failed to send transfer request email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private String getOwnerEmail() {
        // Try to find owner user
        java.util.List<User> allUsers = userDAO.findAll();
        for (User user : allUsers) {
            if (user.getRole() == app.model.UserRole.OWNER) {
                return user.getEmail();
            }
        }
        return null;
    }
    
    private String buildPasswordResetEmailTemplate(String firstName, String newPassword) {
        return "<!DOCTYPE html>" +
            "<html><head><style>" +
            "body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; }" +
            ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
            ".header { text-align: center; color: #764ba2; margin-bottom: 30px; }" +
            ".content { color: #333; line-height: 1.6; }" +
            ".password-box { background: #f5f5f5; border-left: 4px solid #764ba2; padding: 15px; margin: 20px 0; font-family: monospace; font-size: 18px; font-weight: bold; }" +
            ".footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px; }" +
            "</style></head><body>" +
            "<div class='container'>" +
            "<div class='header'><h1>🔐 Password Reset</h1></div>" +
            "<div class='content'>" +
            "<p>Hello " + firstName + ",</p>" +
            "<p>Your password has been successfully reset as requested.</p>" +
            "<p>Your new password is:</p>" +
            "<div class='password-box'>" + newPassword + "</div>" +
            "<p><strong>Important:</strong> Please change this password after logging in for security purposes.</p>" +
            "<p>If you did not request this password reset, please contact the system administrator immediately.</p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated message from Gym Management System</p>" +
            "<p>&copy; 2024 Gym Management System. All rights reserved.</p>" +
            "</div></div></body></html>";
    }
    
    private String buildTransferRequestEmailTemplate(Branch targetBranch, String reason) {
        String currentBranchName = getBranchName(currentUser.getBranchId());
        
        return "<!DOCTYPE html>" +
            "<html><head><style>" +
            "body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; }" +
            ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
            ".header { text-align: center; color: #764ba2; margin-bottom: 30px; }" +
            ".content { color: #333; line-height: 1.6; }" +
            ".info-box { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 5px; padding: 15px; margin: 15px 0; }" +
            ".info-row { display: flex; padding: 8px 0; border-bottom: 1px solid #e9ecef; }" +
            ".info-label { font-weight: bold; width: 150px; color: #666; }" +
            ".info-value { color: #333; flex: 1; }" +
            ".reason-box { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
            ".footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px; }" +
            "</style></head><body>" +
            "<div class='container'>" +
            "<div class='header'><h1>🔄 Branch Transfer Request</h1></div>" +
            "<div class='content'>" +
            "<p>A branch transfer request has been submitted by an admin.</p>" +
            "<div class='info-box'>" +
            "<div class='info-row'><div class='info-label'>Admin Name:</div><div class='info-value'>" + currentUser.getFirstName() + " " + currentUser.getLastName() + "</div></div>" +
            "<div class='info-row'><div class='info-label'>Username:</div><div class='info-value'>" + currentUser.getUsername() + "</div></div>" +
            "<div class='info-row'><div class='info-label'>Email:</div><div class='info-value'>" + currentUser.getEmail() + "</div></div>" +
            "<div class='info-row'><div class='info-label'>Current Branch:</div><div class='info-value'>" + currentBranchName + "</div></div>" +
            "<div class='info-row'><div class='info-label'>Requested Branch:</div><div class='info-value'>" + targetBranch.getName() + " (" + targetBranch.getAddress() + ")</div></div>" +
            "</div>" +
            "<div class='reason-box'>" +
            "<p><strong>Reason:</strong></p>" +
            "<p>" + reason + "</p>" +
            "</div>" +
            "<p>Please review this request and take appropriate action through the system.</p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>This is an automated notification from Gym Management System</p>" +
            "<p>&copy; 2024 Gym Management System. All rights reserved.</p>" +
            "</div></div></body></html>";
    }
    
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    private String getBranchName(Integer branchId) {
        if (branchId == null) {
            return "Not Assigned";
        }
        
        try {
            return branchDAO.findById(branchId)
                .map(branch -> branch.getName() + " (" + branch.getAddress() + ")")
                .orElse("Unknown Branch");
        } catch (Exception e) {
            return "Branch #" + branchId;
        }
    }
    
    // UI Component Creation Methods
    
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent blur panel
                g2d.setColor(new Color(50, 50, 50, 140));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(220, 220, 230));
        return label;
    }
    
    private JTextField createReadOnlyField(String text) {
        JTextField field = new JTextField(text != null ? text : "");
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(450, 45));
        field.setOpaque(true);
        field.setBackground(new Color(45, 45, 50));
        field.setForeground(new Color(220, 220, 220));
        field.setCaretColor(new Color(200, 200, 200));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 80), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setEditable(false);
        field.setFocusable(false);
        return field;
    }
    
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(new Color(220, 220, 220));
        button.setBackground(new Color(60, 60, 70));
        button.setPreferredSize(new Dimension(400, 48));
        button.setMaximumSize(new Dimension(400, 48));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 90));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 70));
            }
        });
        
        return button;
    }
}
