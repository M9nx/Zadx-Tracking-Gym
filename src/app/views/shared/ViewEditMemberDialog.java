package app.views.shared;

import app.model.Member;
import app.model.Member.Gender;
import app.model.User;
import app.service.MemberService;
import app.service.UserService;
import app.util.SessionManager;
import app.util.UIThemeUtil;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;

/**
 * View/Edit Member Dialog - Form for creating or editing member records.
 * 
 * Features:
 * - Auto-generated 8-digit random ID
 * - JSpinner for date selection (alternative to JDateChooser)
 * - Automatic period calculation from payment
 * - Automatic end date calculation
 * - Coach assignment dropdown
 * - Input validation
 * - Glass-morphism design
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class ViewEditMemberDialog extends JDialog {
    
    private final MemberService memberService;
    private final UserService userService;
    private final SessionManager sessionManager;
    private final Member existingMember; // null if creating new
    private final int branchId;
    private boolean saved = false;
    
    // Form fields
    private JTextField randomIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField mobileField;
    private JTextField emailField;
    private JTextField heightField;
    private JTextField weightField;
    private JComboBox<String> genderCombo;
    private JTextField dobField; // Format: yyyy-MM-dd
    private JTextField paymentField;
    private JTextField periodField;
    private JTextField startDateField; // Format: yyyy-MM-dd
    private JTextField endDateField; // Format: yyyy-MM-dd (auto-calculated)
    private JComboBox<User> coachCombo;
    private JCheckBox activeCheckbox;
    
    /**
     * Constructor for creating new member.
     */
    public ViewEditMemberDialog(Window parent, int branchId) {
        this(parent, null, branchId);
    }
    
    /**
     * Constructor for editing existing member.
     */
    public ViewEditMemberDialog(Window parent, Member member, int branchId) {
        super(parent, member == null ? "Add Member" : "Edit Member", ModalityType.APPLICATION_MODAL);
        
        this.memberService = new MemberService();
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance();
        this.existingMember = member;
        this.branchId = branchId;
        
        initComponents();
        loadCoaches();
        
        if (member != null) {
            populateFields(member);
        }
        
        // Don't call pack() because we're using null layout with absolute positioning
        // pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(600, 750);
        setResizable(false);
        
        // Main panel with glass effect
        JPanel mainPanel = UIThemeUtil.createGlassPanel();
        mainPanel.setLayout(null);
        
        int yPos = 20;
        int labelWidth = 150;
        int fieldWidth = 400;
        int fieldHeight = 35;
        int spacing = 45;
        
        // Random ID (auto-generated, read-only for new)
        JLabel randomIdLabel = createLabel("Member ID:", 20, yPos, labelWidth, 25);
        mainPanel.add(randomIdLabel);
        
        randomIdField = UIThemeUtil.createStyledTextField();
        randomIdField.setBounds(180, yPos, fieldWidth, fieldHeight);
        randomIdField.setEditable(existingMember != null); // Only editable when editing
        if (existingMember == null) {
            randomIdField.setText("Auto-generated");
            randomIdField.setForeground(Color.GRAY);
        }
        mainPanel.add(randomIdField);
        yPos += spacing;
        
        // First Name
        JLabel firstNameLabel = createLabel("First Name:*", 20, yPos, labelWidth, 25);
        mainPanel.add(firstNameLabel);
        
        firstNameField = UIThemeUtil.createStyledTextField();
        firstNameField.setBounds(180, yPos, fieldWidth, fieldHeight);
        mainPanel.add(firstNameField);
        yPos += spacing;
        
        // Last Name
        JLabel lastNameLabel = createLabel("Last Name:*", 20, yPos, labelWidth, 25);
        mainPanel.add(lastNameLabel);
        
        lastNameField = UIThemeUtil.createStyledTextField();
        lastNameField.setBounds(180, yPos, fieldWidth, fieldHeight);
        mainPanel.add(lastNameField);
        yPos += spacing;
        
        // Mobile
        JLabel mobileLabel = createLabel("Mobile:*", 20, yPos, labelWidth, 25);
        mainPanel.add(mobileLabel);
        
        mobileField = UIThemeUtil.createStyledTextField();
        mobileField.setBounds(180, yPos, fieldWidth, fieldHeight);
        mainPanel.add(mobileField);
        yPos += spacing;
        
        // Email (optional)
        JLabel emailLabel = createLabel("Email:", 20, yPos, labelWidth, 25);
        mainPanel.add(emailLabel);
        
        emailField = UIThemeUtil.createStyledTextField();
        emailField.setBounds(180, yPos, fieldWidth, fieldHeight);
        mainPanel.add(emailField);
        yPos += spacing;
        
        // Height and Weight (side by side)
        JLabel heightLabel = createLabel("Height (cm):", 20, yPos, labelWidth, 25);
        mainPanel.add(heightLabel);
        
        heightField = UIThemeUtil.createStyledTextField();
        heightField.setBounds(180, yPos, 180, fieldHeight);
        mainPanel.add(heightField);
        
        JLabel weightLabel = createLabel("Weight (kg):", 380, yPos, 80, 25);
        mainPanel.add(weightLabel);
        
        weightField = UIThemeUtil.createStyledTextField();
        weightField.setBounds(480, yPos, 100, fieldHeight);
        mainPanel.add(weightField);
        yPos += spacing;
        
        // Gender
        JLabel genderLabel = createLabel("Gender:*", 20, yPos, labelWidth, 25);
        mainPanel.add(genderLabel);
        
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE"});
        genderCombo.setBounds(180, yPos, fieldWidth, fieldHeight);
        styleComboBox(genderCombo);
        mainPanel.add(genderCombo);
        yPos += spacing;
        
        // Date of Birth
        JLabel dobLabel = createLabel("Date of Birth (yyyy-MM-dd):", 20, yPos, labelWidth, 25);
        mainPanel.add(dobLabel);
        
        dobField = UIThemeUtil.createStyledTextField();
        dobField.setBounds(180, yPos, fieldWidth, fieldHeight);
        dobField.setToolTipText("Format: yyyy-MM-dd (e.g., 2000-01-15)");
        mainPanel.add(dobField);
        yPos += spacing;
        
        // Payment Amount
        JLabel paymentLabel = createLabel("Payment (LE):*", 20, yPos, labelWidth, 25);
        mainPanel.add(paymentLabel);
        
        paymentField = UIThemeUtil.createStyledTextField();
        paymentField.setBounds(180, yPos, 180, fieldHeight);
        paymentField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculatePeriodAndEndDate();
            }
        });
        mainPanel.add(paymentField);
        
        // Period (auto-calculated)
        JLabel periodLabel = createLabel("Period:", 380, yPos, 80, 25);
        mainPanel.add(periodLabel);
        
        periodField = UIThemeUtil.createStyledTextField();
        periodField.setBounds(480, yPos, 100, fieldHeight);
        periodField.setEditable(false);
        periodField.setBackground(new Color(60, 60, 60));
        mainPanel.add(periodField);
        yPos += spacing;
        
        // Start Date
        JLabel startDateLabel = createLabel("Start Date (yyyy-MM-dd):*", 20, yPos, labelWidth, 25);
        mainPanel.add(startDateLabel);
        
        startDateField = UIThemeUtil.createStyledTextField();
        startDateField.setBounds(180, yPos, fieldWidth, fieldHeight);
        startDateField.setText(LocalDate.now().toString()); // Default to today
        startDateField.setToolTipText("Format: yyyy-MM-dd (e.g., 2025-12-09)");
        startDateField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculatePeriodAndEndDate();
            }
        });
        mainPanel.add(startDateField);
        yPos += spacing;
        
        // End Date (auto-calculated)
        JLabel endDateLabel = createLabel("End Date (auto):", 20, yPos, labelWidth, 25);
        mainPanel.add(endDateLabel);
        
        endDateField = UIThemeUtil.createStyledTextField();
        endDateField.setBounds(180, yPos, fieldWidth, fieldHeight);
        endDateField.setEditable(false);
        endDateField.setBackground(new Color(60, 60, 60));
        mainPanel.add(endDateField);
        yPos += spacing;
        
        // Assigned Coach
        JLabel coachLabel = createLabel("Assigned Coach:", 20, yPos, labelWidth, 25);
        mainPanel.add(coachLabel);
        
        coachCombo = new JComboBox<>();
        coachCombo.setBounds(180, yPos, fieldWidth, fieldHeight);
        styleComboBox(coachCombo);
        mainPanel.add(coachCombo);
        yPos += spacing;
        
        // Active checkbox
        activeCheckbox = new JCheckBox("Active Member");
        activeCheckbox.setBounds(180, yPos, 200, 30);
        activeCheckbox.setForeground(Color.WHITE);
        activeCheckbox.setOpaque(false);
        activeCheckbox.setSelected(true);
        mainPanel.add(activeCheckbox);
        yPos += 50;
        
        // Buttons
        JButton saveButton = UIThemeUtil.createStyledButton("SAVE", new Color(50, 150, 50), new Color(70, 170, 70));
        saveButton.setBounds(180, yPos, 180, 45);
        saveButton.addActionListener(e -> saveMember());
        mainPanel.add(saveButton);
        
        JButton cancelButton = UIThemeUtil.createStyledButton("CANCEL", new Color(150, 50, 50), new Color(170, 70, 70));
        cancelButton.setBounds(380, yPos, 180, 45);
        cancelButton.addActionListener(e -> dispose());
        mainPanel.add(cancelButton);
        
        setContentPane(mainPanel);
    }
    
    private JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, width, height);
        return label;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(50, 50, 50));
        combo.setForeground(Color.WHITE);
    }
    
    private void loadCoaches() {
        coachCombo.removeAllItems();
        coachCombo.addItem(null); // Allow no coach assignment
        
        try {
            List<User> coaches = userService.getCoachesByBranch(branchId);
            for (User coach : coaches) {
                coachCombo.addItem(coach);
            }
        } catch (Exception e) {
            System.err.println("Error loading coaches: " + e.getMessage());
        }
    }
    
    private void populateFields(Member member) {
        randomIdField.setText(String.valueOf(member.getRandomId()));
        firstNameField.setText(member.getFirstName());
        lastNameField.setText(member.getLastName());
        mobileField.setText(member.getMobile());
        emailField.setText(member.getEmail() != null ? member.getEmail() : "");
        
        if (member.getHeight() != null) {
            heightField.setText(member.getHeight().toString());
        }
        
        if (member.getWeight() != null) {
            weightField.setText(member.getWeight().toString());
        }
        
        genderCombo.setSelectedItem(member.getGender().name());
        
        if (member.getDateOfBirth() != null) {
            dobField.setText(member.getDateOfBirth().toString());
        }
        
        paymentField.setText(member.getPayment().toString());
        periodField.setText(member.getPeriod());
        startDateField.setText(member.getStartDate().toString());
        endDateField.setText(member.getEndDate().toString());
        
        // Select coach
        if (member.getAssignedCoach() != null) {
            for (int i = 0; i < coachCombo.getItemCount(); i++) {
                User coach = coachCombo.getItemAt(i);
                if (coach != null && coach.getId() == member.getAssignedCoach()) {
                    coachCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        activeCheckbox.setSelected(member.isActive());
    }
    
    private void calculatePeriodAndEndDate() {
        try {
            String paymentText = paymentField.getText().trim();
            String startDateText = startDateField.getText().trim();
            
            if (paymentText.isEmpty() || startDateText.isEmpty()) {
                periodField.setText("");
                endDateField.setText("");
                return;
            }
            
            BigDecimal payment = new BigDecimal(paymentText);
            BigDecimal monthlyPrice = new BigDecimal("150.00");
            LocalDate startDate = LocalDate.parse(startDateText);
            
            // Calculate period
            String period = app.util.DateUtil.calculatePeriod(payment, monthlyPrice);
            periodField.setText(period);
            
            // Calculate end date
            LocalDate endDate = app.util.DateUtil.calculateEndDate(startDate, payment, monthlyPrice);
            endDateField.setText(endDate.toString());
            
        } catch (NumberFormatException e) {
            periodField.setText("Invalid payment");
            endDateField.setText("");
        } catch (java.time.format.DateTimeParseException e) {
            periodField.setText("Invalid date");
            endDateField.setText("");
        }
    }
    
    private void saveMember() {
        try {
            // Validate required fields
            if (firstNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                firstNameField.requestFocus();
                return;
            }
            
            if (lastNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                lastNameField.requestFocus();
                return;
            }
            
            if (mobileField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mobile is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                mobileField.requestFocus();
                return;
            }
            
            if (paymentField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Payment is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                paymentField.requestFocus();
                return;
            }
            
            if (startDateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Start date is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                startDateField.requestFocus();
                return;
            }
            
            // Parse values
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String mobile = mobileField.getText().trim();
            String email = emailField.getText().trim();
            if (email.isEmpty()) email = null;
            
            BigDecimal height = null;
            if (!heightField.getText().trim().isEmpty()) {
                height = new BigDecimal(heightField.getText().trim());
            }
            
            BigDecimal weight = null;
            if (!weightField.getText().trim().isEmpty()) {
                weight = new BigDecimal(weightField.getText().trim());
            }
            
            Gender gender = Gender.valueOf((String) genderCombo.getSelectedItem());
            
            LocalDate dateOfBirth = null;
            String dobText = dobField.getText().trim();
            if (!dobText.isEmpty()) {
                try {
                    dateOfBirth = LocalDate.parse(dobText);
                } catch (java.time.format.DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid date of birth format. Use yyyy-MM-dd", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    dobField.requestFocus();
                    return;
                }
            }
            
            BigDecimal payment = new BigDecimal(paymentField.getText().trim());
            LocalDate startDate;
            try {
                startDate = LocalDate.parse(startDateField.getText().trim());
            } catch (java.time.format.DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid start date format. Use yyyy-MM-dd", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                startDateField.requestFocus();
                return;
            }
            
            User selectedCoach = (User) coachCombo.getSelectedItem();
            Integer coachId = selectedCoach != null ? selectedCoach.getId() : null;
            
            boolean isActive = activeCheckbox.isSelected();
            
            String ipAddress = sessionManager.getCurrentUser() != null ? "127.0.0.1" : "unknown";
            
            boolean success;
            if (existingMember == null) {
                // Create new member
                success = memberService.createMember(
                    firstName, lastName, mobile, email, height, weight,
                    gender, dateOfBirth, payment, startDate, coachId, branchId, ipAddress
                ).isPresent();
            } else {
                // Update existing member
                success = memberService.updateMember(
                    existingMember.getId(), firstName, lastName, mobile, email,
                    height, weight, gender, dateOfBirth, payment, startDate,
                    coachId, branchId, isActive, ipAddress
                );
            }
            
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, 
                    existingMember == null ? "Member created successfully!" : "Member updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to save member. Please check your inputs and try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid number format. Please check height, weight, and payment fields.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving member: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}
