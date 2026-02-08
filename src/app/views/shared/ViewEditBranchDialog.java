package app.views.shared;

import app.model.Branch;
import app.service.BranchService;
import app.util.SessionManager;
import app.util.UIThemeUtil;
import app.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Branch Create/Edit Dialog
 * Validates branch name, address, phone
 * Enforces unique branch names
 */
public class ViewEditBranchDialog extends JDialog {
    
    private final BranchService branchService;
    private final SessionManager sessionManager;
    private final Branch existingBranch;
    
    private JTextField branchNameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JCheckBox activeCheckBox;
    private JButton saveButton, cancelButton;
    
    public ViewEditBranchDialog(Window parent, Branch branch) {
        super(parent, branch == null ? "Add Branch" : "Edit Branch", ModalityType.APPLICATION_MODAL);
        this.branchService = new BranchService();
        this.sessionManager = SessionManager.getInstance();
        this.existingBranch = branch;
        
        initComponents();
        
        if (branch != null) {
            populateFields(branch);
        }
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        // Main panel with glass-morphism
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIThemeUtil.OWNER_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Form panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel);
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Branch Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel branchNameLabel = new JLabel("Branch Name:");
        branchNameLabel.setForeground(Color.WHITE);
        branchNameLabel.setFont(UIThemeUtil.LABEL_FONT);
        formPanel.add(branchNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        branchNameField = new JTextField();
        branchNameField.setFont(UIThemeUtil.INPUT_FONT);
        formPanel.add(branchNameField, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setForeground(Color.WHITE);
        addressLabel.setFont(UIThemeUtil.LABEL_FONT);
        formPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        addressField = new JTextField();
        addressField.setFont(UIThemeUtil.INPUT_FONT);
        formPanel.add(addressField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setForeground(Color.WHITE);
        phoneLabel.setFont(UIThemeUtil.LABEL_FONT);
        formPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        phoneField = new JTextField();
        phoneField.setFont(UIThemeUtil.INPUT_FONT);
        formPanel.add(phoneField, gbc);
        
        // Active checkbox
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel activeLabel = new JLabel("Active:");
        activeLabel.setForeground(Color.WHITE);
        activeLabel.setFont(UIThemeUtil.LABEL_FONT);
        formPanel.add(activeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        activeCheckBox = new JCheckBox();
        activeCheckBox.setOpaque(false);
        activeCheckBox.setSelected(true); // Default to active
        formPanel.add(activeCheckBox, gbc);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        saveButton = UIThemeUtil.createStyledButton("SAVE", UIThemeUtil.SUCCESS_COLOR, UIThemeUtil.SUCCESS_COLOR.darker());
        saveButton.addActionListener(e -> saveBranch());
        buttonPanel.add(saveButton);
        
        cancelButton = UIThemeUtil.createStyledButton("CANCEL", new Color(108, 117, 125), new Color(90, 98, 104));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        return buttonPanel;
    }
    
    private void populateFields(Branch branch) {
        branchNameField.setText(branch.getBranchName());
        addressField.setText(branch.getAddress());
        phoneField.setText(branch.getPhone());
        activeCheckBox.setSelected(branch.isActive());
    }
    
    private void saveBranch() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        try {
            String branchName = branchNameField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneField.getText().trim();
            boolean isActive = activeCheckBox.isSelected();
            
            String ipAddress = "127.0.0.1"; // TODO: Get actual IP
            
            if (existingBranch == null) {
                // Create new branch
                branchService.createBranch(branchName, address, phone, ipAddress);
                
                JOptionPane.showMessageDialog(this,
                    "Branch created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update existing branch
                branchService.updateBranch(
                    existingBranch.getBranchId(),
                    branchName,
                    address,
                    phone,
                    isActive,
                    ipAddress
                );
                
                JOptionPane.showMessageDialog(this,
                    "Branch updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            dispose();
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving branch: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean validateInputs() {
        // Validate branch name
        if (branchNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Branch name is required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            branchNameField.requestFocus();
            return false;
        }
        
        // Validate address
        if (addressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Address is required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            addressField.requestFocus();
            return false;
        }
        
        // Validate phone format
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Phone is required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidMobile(phone)) {
            JOptionPane.showMessageDialog(this,
                "Invalid phone format. Expected: 01XXXXXXXXX",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        return true;
    }
}
