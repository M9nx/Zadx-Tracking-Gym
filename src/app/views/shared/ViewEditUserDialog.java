package app.views.shared;

import app.model.User;
import app.model.UserRole;
import app.model.Branch;
import app.service.UserService;
import app.service.BranchService;
import app.util.UIThemeUtil;
import app.util.ValidationUtil;
import app.util.PasswordUtil;
import app.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View/Edit User Dialog - Form for creating or editing user accounts.
 * 
 * Features:
 * - Role selection (Admin/Coach only - Owner created manually)
 * - Branch assignment (required for Admin/Coach)
 * - Password complexity validation with live feedback
 * - Username/email uniqueness checks
 * - Input validation
 * - Glass-morphism design
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class ViewEditUserDialog extends JDialog {
    
    private final UserService userService;
    private final BranchService branchService;
    private final SessionManager sessionManager;
    private final User existingUser; // null if creating new
    private final UserRole filterRole; // Filter roles for specific context (Admin/Coach page)
    private boolean saved = false;
    
    // Form fields
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField mobileField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordStrengthLabel;
    private JComboBox<UserRole> roleCombo;
    private JComboBox<Branch> branchCombo;
    private JCheckBox activeCheckbox;
    
    /**
     * Constructor for creating new user (all roles).
     */
    public ViewEditUserDialog(Window parent) {
        this(parent, null, null);
    }
    
    /**
     * Constructor for creating new user with role filter.
     */
    public ViewEditUserDialog(Window parent, UserRole filterRole) {
        this(parent, null, filterRole);
    }
    
    /**
     * Constructor for editing existing user.
     */
    public ViewEditUserDialog(Window parent, User user) {
        this(parent, user, null);
    }
    
    /**
     * Main constructor with all parameters.
     */
    public ViewEditUserDialog(Window parent, User user, UserRole filterRole) {
        super(parent, user == null ? 
            (filterRole == UserRole.ADMIN ? "Add New Admin" : 
             filterRole == UserRole.COACH ? "Add New Coach" : "Add New User") : 
            "Edit User", 
            ModalityType.APPLICATION_MODAL);
        
        this.userService = new UserService();
        this.branchService = new BranchService();
        this.sessionManager = SessionManager.getInstance();
        this.existingUser = user;
        this.filterRole = filterRole;
        
        initComponents();
        loadBranches();
        
        if (user != null) {
            populateFields(user);
        }
        
        setLocationRelativeTo(parent);
        setVisible(false); // Don't show yet
    }
    
    private void initComponents() {
        setSize(750, 920);
        setResizable(false);
        getContentPane().setBackground(new Color(15, 15, 20));
        
        // Main panel with glass effect
        JPanel mainPanel = UIThemeUtil.createGlassPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(710, 880));
        mainPanel.setBounds(20, 20, 710, 880);
        mainPanel.setOpaque(false);
        
        // Add title
        JLabel titleLabel = new JLabel(existingUser == null ? 
            (filterRole == UserRole.ADMIN ? "Add New Admin" : 
             filterRole == UserRole.COACH ? "Add New Coach" : "Add New User") : 
            "Edit User");
        titleLabel.setBounds(0, 15, 710, 35);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(150, 120, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel);
        
        int yPos = 65;
        int labelX = 40;
        int labelWidth = 180;
        int fieldX = 230;
        int fieldWidth = 440;
        int fieldHeight = 48;
        int spacing = 63;
        
        // Username
        JLabel usernameLabel = createLabel("Username:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(usernameLabel);
        
        usernameField = UIThemeUtil.createStyledTextField();
        usernameField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        usernameField.setEnabled(existingUser == null);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        mainPanel.add(usernameField);
        yPos += spacing;
        
        // First Name
        JLabel firstNameLabel = createLabel("First Name:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(firstNameLabel);
        
        firstNameField = UIThemeUtil.createStyledTextField();
        firstNameField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        firstNameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        firstNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        mainPanel.add(firstNameField);
        yPos += spacing;
        
        // Last Name
        JLabel lastNameLabel = createLabel("Last Name:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(lastNameLabel);
        
        lastNameField = UIThemeUtil.createStyledTextField();
        lastNameField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        lastNameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lastNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        mainPanel.add(lastNameField);
        yPos += spacing;
        
        // Email
        JLabel emailLabel = createLabel("Email:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(emailLabel);
        
        emailField = UIThemeUtil.createStyledTextField();
        emailField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        emailField.setToolTipText("example@domain.com");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        mainPanel.add(emailField);
        yPos += spacing;
        
        // Mobile
        JLabel mobileLabel = createLabel("Mobile:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(mobileLabel);
        
        mobileField = UIThemeUtil.createStyledTextField();
        mobileField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        mobileField.setToolTipText("Format: 01XXXXXXXXX (Egyptian format)");
        mobileField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        mobileField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        mainPanel.add(mobileField);
        yPos += spacing;
        
        // Password (only for new users or password change)
        JLabel passwordLabel = createLabel(existingUser == null ? "Password:*" : "New Password:", labelX, yPos, labelWidth, 35);
        mainPanel.add(passwordLabel);
        
        passwordField = UIThemeUtil.createStyledPasswordField();
        passwordField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validatePasswordStrength();
            }
        });
        addTabNavigation(passwordField);
        mainPanel.add(passwordField);
        yPos += spacing;
        
        // Confirm Password
        JLabel confirmPasswordLabel = createLabel("Confirm Password:", labelX, yPos, labelWidth, 35);
        mainPanel.add(confirmPasswordLabel);
        
        confirmPasswordField = UIThemeUtil.createStyledPasswordField();
        confirmPasswordField.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        addTabNavigation(confirmPasswordField);
        mainPanel.add(confirmPasswordField);
        yPos += spacing;
        
        // Password strength indicator
        passwordStrengthLabel = new JLabel("");
        passwordStrengthLabel.setBounds(fieldX, yPos - 50, fieldWidth, 30);
        passwordStrengthLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        mainPanel.add(passwordStrengthLabel);
        
        // Role (filtered based on context)
        JLabel roleLabel = createLabel("Role:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(roleLabel);
        
        // Filter roles based on context
        UserRole[] availableRoles;
        if (filterRole != null) {
            // Only show the specific role for this context
            availableRoles = new UserRole[]{filterRole};
        } else {
            // Show both Admin and Coach
            availableRoles = new UserRole[]{UserRole.ADMIN, UserRole.COACH};
        }
        
        roleCombo = new JComboBox<>(availableRoles);
        roleCombo.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        roleCombo.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true));
        styleComboBox(roleCombo);
        roleCombo.addActionListener(e -> updateBranchRequirement());
        mainPanel.add(roleCombo);
        yPos += spacing;
        
        // Branch
        JLabel branchLabel = createLabel("Branch:*", labelX, yPos, labelWidth, 35);
        mainPanel.add(branchLabel);
        
        branchCombo = new JComboBox<>();
        branchCombo.setBounds(fieldX, yPos, fieldWidth, fieldHeight);
        branchCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        branchCombo.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true));
        branchCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Branch) {
                    setText(((Branch) value).getBranchName());
                }
                setFont(new Font("Segoe UI", Font.PLAIN, 16));
                return this;
            }
        });
        styleComboBox(branchCombo);
        mainPanel.add(branchCombo);
        yPos += spacing;
        
        // Active checkbox
        activeCheckbox = new JCheckBox("Active User");
        activeCheckbox.setBounds(fieldX, yPos, 250, 40);
        activeCheckbox.setForeground(new Color(200, 200, 210));
        activeCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activeCheckbox.setOpaque(false);
        activeCheckbox.setSelected(true);
        mainPanel.add(activeCheckbox);
        yPos += 60;
        
        // Buttons
        int buttonWidth = 210;
        int buttonHeight = 52;
        int buttonSpacing = 20;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int buttonStartX = (710 - totalButtonWidth) / 2;
        
        JButton saveButton = UIThemeUtil.createStyledButton("SAVE", new Color(50, 150, 50), new Color(70, 170, 70));
        saveButton.setBounds(buttonStartX, yPos, buttonWidth, buttonHeight);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        saveButton.addActionListener(e -> saveUser());
        mainPanel.add(saveButton);
        
        JButton cancelButton = UIThemeUtil.createStyledButton("CANCEL", new Color(150, 50, 50), new Color(170, 70, 70));
        cancelButton.setBounds(buttonStartX + buttonWidth + buttonSpacing, yPos, buttonWidth, buttonHeight);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.addActionListener(e -> dispose());
        mainPanel.add(cancelButton);
        
        // Create a container panel for the glasspanel
        JPanel container = new JPanel(null);
        container.setBackground(new Color(15, 15, 20));
        container.add(mainPanel);
        
        setContentPane(container);
    }
    
    private JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(220, 220, 230));
        label.setBounds(x, y, width, height);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        combo.setBackground(new Color(50, 50, 55));
        combo.setForeground(Color.WHITE);
    }
    
    private void loadBranches() {
        branchCombo.removeAllItems();
        
        try {
            List<Branch> branches = branchService.getActiveBranches();
            for (Branch branch : branches) {
                branchCombo.addItem(branch);
            }
        } catch (Exception e) {
            System.err.println("Error loading branches: " + e.getMessage());
        }
    }
    
    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        usernameField.setEditable(false); // Username cannot be changed
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        mobileField.setText(user.getMobile());
        
        roleCombo.setSelectedItem(user.getRole());
        
        // Select branch
        if (user.getBranchId() != null) {
            for (int i = 0; i < branchCombo.getItemCount(); i++) {
                Branch branch = branchCombo.getItemAt(i);
                if (branch != null && branch.getBranchId() == user.getBranchId()) {
                    branchCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        activeCheckbox.setSelected(user.isActive());
    }
    
    private void updateBranchRequirement() {
        UserRole selectedRole = (UserRole) roleCombo.getSelectedItem();
        branchCombo.setEnabled(selectedRole != UserRole.OWNER);
    }
    
    private void validatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        
        if (password.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }
        
        ValidationUtil.ValidationResult result = PasswordUtil.validatePasswordComplexity(password);
        
        if (result.isValid()) {
            passwordStrengthLabel.setText("✓ Strong password");
            passwordStrengthLabel.setForeground(new Color(50, 200, 50));
        } else {
            passwordStrengthLabel.setText("✗ " + result.getMessage());
            passwordStrengthLabel.setForeground(new Color(255, 100, 100));
        }
    }
    
    private void saveUser() {
        try {
            // Validate required fields
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                showError("Username is required");
                usernameField.requestFocus();
                return;
            }
            
            String firstName = firstNameField.getText().trim();
            if (firstName.isEmpty()) {
                showError("First name is required");
                firstNameField.requestFocus();
                return;
            }
            
            String lastName = lastNameField.getText().trim();
            if (lastName.isEmpty()) {
                showError("Last name is required");
                lastNameField.requestFocus();
                return;
            }
            
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showError("Email is required");
                emailField.requestFocus();
                return;
            }
            
            String mobile = mobileField.getText().trim();
            if (mobile.isEmpty()) {
                showError("Mobile is required");
                mobileField.requestFocus();
                return;
            }
            
            // Validate password for new users
            String password = new String(passwordField.getPassword());
            if (existingUser == null && password.isEmpty()) {
                showError("Password is required for new users");
                passwordField.requestFocus();
                return;
            }
            
            // Validate password if provided
            if (!password.isEmpty()) {
                String confirmPassword = new String(confirmPasswordField.getPassword());
                if (!password.equals(confirmPassword)) {
                    showError("Passwords do not match");
                    confirmPasswordField.requestFocus();
                    return;
                }
                
                ValidationUtil.ValidationResult passwordResult = PasswordUtil.validatePasswordComplexity(password);
                if (!passwordResult.isValid()) {
                    showError("Password is too weak:\n" + passwordResult.getMessage());
                    passwordField.requestFocus();
                    return;
                }
            }
            
            UserRole role = (UserRole) roleCombo.getSelectedItem();
            Branch selectedBranch = (Branch) branchCombo.getSelectedItem();
            
            if (selectedBranch == null && role != UserRole.OWNER) {
                showError("Branch is required for Admin and Coach roles");
                return;
            }
            
            Integer branchId = selectedBranch != null ? selectedBranch.getBranchId() : null;
            boolean isActive = activeCheckbox.isSelected();
            String ipAddress = "127.0.0.1"; // TODO: Get real IP
            
            boolean success;
            if (existingUser == null) {
                // Create new user
                try {
                    java.util.Optional<User> createdUser = userService.createUser(
                        firstName, lastName, username, email, password, mobile,
                        role, branchId, ipAddress
                    );
                    success = createdUser.isPresent();
                    
                    if (!success) {
                        showError("Failed to create user. The username or email might already exist.");
                        return;
                    }
                } catch (Exception ex) {
                    showError("Error creating user: " + ex.getMessage());
                    ex.printStackTrace();
                    return;
                }
            } else {
                // Update existing user
                try {
                    success = userService.updateUser(
                        existingUser.getId(), firstName, lastName, email, mobile,
                        role, branchId, isActive, ipAddress
                    );
                    
                    if (!success) {
                        showError("Failed to update user. Please check your inputs.");
                        return;
                    }
                } catch (Exception ex) {
                    showError("Error updating user: " + ex.getMessage());
                    ex.printStackTrace();
                    return;
                }
            }
            
            // Save successful
            saved = true;
            JOptionPane.showMessageDialog(this,
                existingUser == null ? "User created successfully!" : "User updated successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception e) {
            showError("Error saving user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    /**
     * Adds Enter key listener to move focus to next component (like Tab key).
     */
    private void addTabNavigation(JComponent component) {
        component.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    // Move focus to next component
                    component.transferFocus();
                }
            }
        });
    }
}
