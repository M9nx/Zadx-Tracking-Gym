package app.views.coach;

import app.dao.BranchDAO;
import app.dao.UserDAO;
import app.model.User;
import app.util.EmailService;
import app.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;

/**
 * CoachProfileView - Coach profile page with read-only information
 * Features:
 * - Display current name, email, branch
 * - Password reset (sends new password to email)
 * - Read-only access (no editing)
 */
public class CoachProfileView extends JPanel {
    
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
    
    public CoachProfileView(User user) {
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
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Coach Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(220, 220, 230));
        
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
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(createLabel("First Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        firstNameField = createReadOnlyField(currentUser.getFirstName());
        panel.add(firstNameField, gbc);
        row++;
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(createLabel("Last Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        lastNameField = createReadOnlyField(currentUser.getLastName());
        panel.add(lastNameField, gbc);
        row++;
        
        // Username
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        usernameField = createReadOnlyField(currentUser.getUsername());
        panel.add(usernameField, gbc);
        row++;
        
        // Email
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        emailField = createReadOnlyField(currentUser.getEmail());
        panel.add(emailField, gbc);
        row++;
        
        // Role
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(createLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        roleField = createReadOnlyField(currentUser.getRole().toString());
        panel.add(roleField, gbc);
        row++;
        
        // Branch
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(createLabel("Current Branch:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
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
        
        // Reset Password Button
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        JButton resetPasswordBtn = createActionButton("Reset My Password");
        resetPasswordBtn.addActionListener(e -> resetPassword());
        panel.add(resetPasswordBtn, gbc);
        
        // Info Label
        gbc.gridy = 1;
        JLabel infoLabel = new JLabel("<html><i>Note: Coaches have read-only access. Contact your branch admin for changes.</i></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(180, 180, 190));
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    private void resetPassword() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "A new password will be generated and sent to your email.\n" +
            "Continue?",
            "Reset Password",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Generate new password
            String newPassword = generateRandomPassword();
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            // Update in database
            boolean updated = userDAO.updatePassword(currentUser.getUserId(), hashedPassword);
            
            if (updated) {
                // Send email
                boolean emailSent = emailService.sendPasswordResetEmail(
                    currentUser.getEmail(),
                    currentUser.getFirstName(),
                    newPassword
                );
                
                if (emailSent) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Your password has been reset!\n" +
                        "A new password has been sent to: " + currentUser.getEmail(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Password reset but email failed to send.\n" +
                        "Your new password is: " + newPassword + "\n" +
                        "Please save it securely!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to reset password. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error resetting password: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    private String getBranchName(Integer branchId) {
        if (branchId == null) {
            return "No Branch Assigned";
        }
        try {
            return branchDAO.findById(branchId)
                .map(branch -> branch.getBranchName())
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
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
