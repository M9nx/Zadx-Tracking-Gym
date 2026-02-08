package app.views.owner;

import app.dao.SystemSettingsDAO;
import app.model.User;
import app.service.AuditService;
import app.util.SessionManager;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * System Settings View - Comprehensive system configuration panel
 * Allows owner to configure gym-wide settings
 */
public class SystemSettingsView extends JPanel {
    
    private final SystemSettingsDAO settingsDAO;
    private final AuditService auditService;
    
    // General Settings
    private JTextField gymNameField;
    private JTextField gymEmailField;
    private JTextField gymPhoneField;
    private JTextArea gymAddressArea;
    
    // Business Settings
    private JSpinner membershipValiditySpinner;
    private JSpinner expirationWarningSpinner;
    private JCheckBox autoRenewalCheckBox;
    private JCheckBox sendEmailNotificationsCheckBox;
    private JCheckBox sendSMSNotificationsCheckBox;
    
    // Payment Settings
    private JTextField monthlyFeeField;
    private JTextField quarterlyFeeField;
    private JTextField biannualFeeField;
    private JTextField annualFeeField;
    private JTextField currencyField;
    private JSpinner lateFeePercentageSpinner;
    
    // Security Settings
    private JSpinner sessionTimeoutSpinner;
    private JSpinner passwordMinLengthSpinner;
    private JCheckBox requirePasswordChangeCheckBox;
    private JSpinner passwordChangeIntervalSpinner;
    private JCheckBox enableAuditLoggingCheckBox;
    
    // Appearance Settings
    private JComboBox<String> themeComboBox;
    private JComboBox<String> languageComboBox;
    private JComboBox<String> dateFormatComboBox;
    private JComboBox<String> timeFormatComboBox;
    
    // Email Configuration Settings
    private JTextField senderEmailField;
    private JTextField smtpHostField;
    private JSpinner smtpPortSpinner;
    private JTextField smtpUsernameField;
    private JPasswordField smtpPasswordField;
    private JCheckBox smtpTLSCheckBox;
    
    // Backup Settings
    private JCheckBox autoBackupCheckBox;
    private JComboBox<String> backupFrequencyComboBox;
    private JTextField backupPathField;
    private JSpinner backupRetentionSpinner;
    
    public SystemSettingsView() {
        this.settingsDAO = new SystemSettingsDAO();
        this.auditService = new AuditService();
        
        setLayout(new BorderLayout());
        setOpaque(true);
        
        initComponents();
        loadSettings();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Try to load background image, fallback to gradient
        try {
            java.awt.Image img = new javax.swing.ImageIcon(
                getClass().getResource("/Images/ambitious-studio-rick-barrett-1RNQ11ZODJM-unsplash.jpg")
            ).getImage();
            g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        } catch (Exception e) {
            // Fallback gradient background
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 20, 25),
                0, getHeight(), new Color(10, 10, 15)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        g2d.dispose();
    }
    
    private void initComponents() {
        // Main content panel with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Settings sections
        mainPanel.add(createGeneralSettingsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createBusinessSettingsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createPaymentSettingsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createSecuritySettingsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createEmailConfigurationSection());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createAppearanceSettingsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createBackupSettingsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Action buttons at bottom
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(35, 35, 40, 220));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(52, 152, 219, 100)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("System Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Configure gym-wide system settings and preferences");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(150, 150, 150));
        
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.CENTER);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createGeneralSettingsSection() {
        JPanel section = createSectionPanel("General Information", "Basic gym details and contact information");
        
        gymNameField = new JTextField();
        gymEmailField = new JTextField();
        gymPhoneField = new JTextField();
        gymAddressArea = new JTextArea(3, 20);
        gymAddressArea.setLineWrap(true);
        gymAddressArea.setWrapStyleWord(true);
        
        // Add tab navigation
        addEnterKeyNavigation(gymNameField);
        addEnterKeyNavigation(gymEmailField);
        addEnterKeyNavigation(gymPhoneField);
        
        JScrollPane addressScroll = new JScrollPane(gymAddressArea);
        addressScroll.setPreferredSize(new Dimension(400, 90));
        addressScroll.setBorder(new RoundedBorder(new Color(80, 80, 90), 1, 10));
        addressScroll.setOpaque(false);
        addressScroll.getViewport().setOpaque(false);
        
        section.add(createFieldRow("Gym Name:", gymNameField));
        section.add(Box.createVerticalStrut(10));
        section.add(createFieldRow("Email Address:", gymEmailField));
        section.add(Box.createVerticalStrut(10));
        section.add(createFieldRow("Phone Number:", gymPhoneField));
        section.add(Box.createVerticalStrut(10));
        section.add(createFieldRow("Physical Address:", addressScroll));
        
        return section;
    }
    
    private JPanel createBusinessSettingsSection() {
        JPanel section = createSectionPanel("Business Rules", "Membership and notification policies");
        
        membershipValiditySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        expirationWarningSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));
        autoRenewalCheckBox = new JCheckBox("Enable automatic membership renewal");
        sendEmailNotificationsCheckBox = new JCheckBox("Send email notifications to members");
        sendSMSNotificationsCheckBox = new JCheckBox("Send SMS notifications to members");
        
        styleCheckBox(autoRenewalCheckBox);
        styleCheckBox(sendEmailNotificationsCheckBox);
        styleCheckBox(sendSMSNotificationsCheckBox);
        
        section.add(createFieldRow("Default Membership Validity (days):", membershipValiditySpinner));
        section.add(createFieldRow("Expiration Warning Period (days):", expirationWarningSpinner));
        section.add(createCheckBoxRow(autoRenewalCheckBox));
        section.add(createCheckBoxRow(sendEmailNotificationsCheckBox));
        section.add(createCheckBoxRow(sendSMSNotificationsCheckBox));
        
        return section;
    }
    
    private JPanel createPaymentSettingsSection() {
        JPanel section = createSectionPanel("Payment Configuration", "Membership fees and payment options");
        
        monthlyFeeField = new JTextField();
        quarterlyFeeField = new JTextField();
        biannualFeeField = new JTextField();
        annualFeeField = new JTextField();
        currencyField = new JTextField();
        lateFeePercentageSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 50.0, 0.5));
        
        section.add(createFieldRow("Monthly Fee:", monthlyFeeField));
        section.add(createFieldRow("Quarterly Fee (3 months):", quarterlyFeeField));
        section.add(createFieldRow("Biannual Fee (6 months):", biannualFeeField));
        section.add(createFieldRow("Annual Fee (12 months):", annualFeeField));
        section.add(createFieldRow("Currency Code:", currencyField));
        section.add(createFieldRow("Late Payment Fee (%):", lateFeePercentageSpinner));
        
        return section;
    }
    
    private JPanel createSecuritySettingsSection() {
        JPanel section = createSectionPanel("Security & Access", "Security policies and access control");
        
        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
        passwordMinLengthSpinner = new JSpinner(new SpinnerNumberModel(8, 4, 20, 1));
        requirePasswordChangeCheckBox = new JCheckBox("Require periodic password changes");
        passwordChangeIntervalSpinner = new JSpinner(new SpinnerNumberModel(90, 30, 365, 30));
        enableAuditLoggingCheckBox = new JCheckBox("Enable audit logging for all actions");
        
        styleCheckBox(requirePasswordChangeCheckBox);
        styleCheckBox(enableAuditLoggingCheckBox);
        
        section.add(createFieldRow("Session Timeout (minutes):", sessionTimeoutSpinner));
        section.add(createFieldRow("Minimum Password Length:", passwordMinLengthSpinner));
        section.add(createCheckBoxRow(requirePasswordChangeCheckBox));
        section.add(createFieldRow("Password Change Interval (days):", passwordChangeIntervalSpinner));
        section.add(createCheckBoxRow(enableAuditLoggingCheckBox));
        
        return section;
    }
    
    private JPanel createEmailConfigurationSection() {
        JPanel section = createSectionPanel("Email Configuration", "Email sender settings for notifications and reports");
        
        senderEmailField = new JTextField();
        smtpHostField = new JTextField();
        smtpPortSpinner = new JSpinner(new SpinnerNumberModel(587, 1, 65535, 1));
        smtpUsernameField = new JTextField();
        smtpPasswordField = new JPasswordField();
        smtpTLSCheckBox = new JCheckBox("Use TLS/SSL encryption");
        
        styleCheckBox(smtpTLSCheckBox);
        styleField(smtpPasswordField);
        
        section.add(createFieldRow("Sender Email Address:", senderEmailField));
        section.add(createFieldRow("SMTP Host:", smtpHostField));
        section.add(createFieldRow("SMTP Port:", smtpPortSpinner));
        section.add(createFieldRow("SMTP Username:", smtpUsernameField));
        section.add(createFieldRow("SMTP Password:", smtpPasswordField));
        section.add(createCheckBoxRow(smtpTLSCheckBox));
        
        return section;
    }
    
    private JPanel createAppearanceSettingsSection() {
        JPanel section = createSectionPanel("Appearance & Localization", "Visual theme and regional settings");
        
        themeComboBox = new JComboBox<>(new String[]{"Dark Theme", "Light Theme", "Auto (System)"});
        languageComboBox = new JComboBox<>(new String[]{"English", "Arabic", "French", "Spanish"});
        dateFormatComboBox = new JComboBox<>(new String[]{"DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"});
        timeFormatComboBox = new JComboBox<>(new String[]{"24-hour", "12-hour (AM/PM)"});
        
        section.add(createFieldRow("Theme:", themeComboBox));
        section.add(createFieldRow("Language:", languageComboBox));
        section.add(createFieldRow("Date Format:", dateFormatComboBox));
        section.add(createFieldRow("Time Format:", timeFormatComboBox));
        
        return section;
    }
    
    private JPanel createBackupSettingsSection() {
        JPanel section = createSectionPanel("Backup & Recovery", "Automated backup configuration");
        
        autoBackupCheckBox = new JCheckBox("Enable automatic database backups");
        backupFrequencyComboBox = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"});
        backupPathField = new JTextField();
        backupRetentionSpinner = new JSpinner(new SpinnerNumberModel(30, 7, 365, 1));
        
        styleCheckBox(autoBackupCheckBox);
        
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseBackupPath());
        styleButton(browseButton);
        
        JPanel pathPanel = new JPanel(new BorderLayout(10, 0));
        pathPanel.setOpaque(false);
        pathPanel.add(backupPathField, BorderLayout.CENTER);
        pathPanel.add(browseButton, BorderLayout.EAST);
        
        section.add(createCheckBoxRow(autoBackupCheckBox));
        section.add(createFieldRow("Backup Frequency:", backupFrequencyComboBox));
        section.add(createFieldRow("Backup Location:", pathPanel));
        section.add(createFieldRow("Retention Period (days):", backupRetentionSpinner));
        
        return section;
    }
    
    private JPanel createSectionPanel(String title, String description) {
        JPanel section = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism blur effect with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(50, 50, 55, 220),
                    0, getHeight(), new Color(40, 40, 45, 200)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Subtle border
                g2d.setColor(new Color(80, 80, 90, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);
                
                // Top highlight
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(2, 2, getWidth()-4, 40, 15, 15);
                
                g2d.dispose();
            }
        };
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(100, 180, 255));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(170, 170, 180));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(8));
        section.add(descLabel);
        section.add(Box.createVerticalStrut(20));
        
        return section;
    }
    
    private JPanel createFieldRow(String label, Component field) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        labelComp.setForeground(new Color(220, 220, 230));
        labelComp.setPreferredSize(new Dimension(180, 48));
        labelComp.setVerticalAlignment(SwingConstants.CENTER);
        
        styleField(field);
        
        row.add(labelComp, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        
        return row;
    }
    
    private JPanel createCheckBoxRow(JCheckBox checkBox) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        row.add(checkBox);
        return row;
    }
    
    private void styleField(Component field) {
        if (field instanceof JTextField) {
            JTextField textField = (JTextField) field;
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            textField.setBackground(new Color(65, 65, 72));
            textField.setForeground(new Color(245, 245, 250));
            textField.setCaretColor(new Color(100, 180, 255));
            textField.setPreferredSize(new Dimension(450, 48));
            textField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(90, 90, 100), 2, 12),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
            ));
        } else if (field instanceof JTextArea) {
            JTextArea textArea = (JTextArea) field;
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            textArea.setBackground(new Color(65, 65, 72));
            textArea.setForeground(new Color(245, 245, 250));
            textArea.setCaretColor(new Color(100, 180, 255));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(90, 90, 100), 2, 12),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            ));
        } else if (field instanceof JPasswordField) {
            JPasswordField passwordField = (JPasswordField) field;
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            passwordField.setBackground(new Color(65, 65, 72));
            passwordField.setForeground(new Color(245, 245, 250));
            passwordField.setCaretColor(new Color(100, 180, 255));
            passwordField.setPreferredSize(new Dimension(450, 48));
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(90, 90, 100), 2, 12),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
            ));
        } else if (field instanceof JSpinner) {
            JSpinner spinner = (JSpinner) field;
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            spinner.setPreferredSize(new Dimension(150, 45));
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField spinnerField = ((JSpinner.DefaultEditor) editor).getTextField();
                spinnerField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                spinnerField.setBackground(new Color(60, 60, 65));
                spinnerField.setForeground(new Color(240, 240, 240));
                spinnerField.setCaretColor(new Color(52, 152, 219));
                spinnerField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            }
        } else if (field instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) field;
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            comboBox.setBackground(new Color(60, 60, 65));
            comboBox.setForeground(new Color(240, 240, 240));
            comboBox.setPreferredSize(new Dimension(300, 45));
            comboBox.setBorder(new RoundedBorder(new Color(80, 80, 90), 1, 10));
        } else if (field instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) field;
            scrollPane.setPreferredSize(new Dimension(450, 90));
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
        } else if (field instanceof JPanel) {
            // Handle panel fields (like backup path with browse button)
            field.setPreferredSize(new Dimension(400, 45));
        }
    }
    
    private void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkBox.setForeground(Color.WHITE);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));
        button.setBorder(new RoundedBorder(new Color(52, 152, 219, 0), 0, 8));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(35, 35, 40, 220));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(52, 152, 219, 100)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JButton saveButton = new JButton("Save All Settings");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension(160, 40));
        saveButton.addActionListener(e -> saveSettings());
        
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setForeground(Color.WHITE);
        resetButton.setBackground(new Color(230, 126, 34));
        resetButton.setBorderPainted(false);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.setPreferredSize(new Dimension(160, 40));
        resetButton.addActionListener(e -> resetToDefaults());
        
        JButton testButton = new JButton("Test Email");
        testButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        testButton.setForeground(Color.WHITE);
        testButton.setBackground(new Color(155, 89, 182));
        testButton.setBorderPainted(false);
        testButton.setFocusPainted(false);
        testButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        testButton.setPreferredSize(new Dimension(120, 40));
        testButton.addActionListener(e -> testEmailSettings());
        
        panel.add(testButton);
        panel.add(resetButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    private void loadSettings() {
        Map<String, String> settings = settingsDAO.getAll();
        
        // General Settings
        gymNameField.setText(settings.getOrDefault("gym.name", "My Gym"));
        gymEmailField.setText(settings.getOrDefault("gym.email", "info@mygym.com"));
        gymPhoneField.setText(settings.getOrDefault("gym.phone", "+20 100 000 0000"));
        gymAddressArea.setText(settings.getOrDefault("gym.address", "123 Main Street, Cairo, Egypt"));
        
        // Business Settings
        membershipValiditySpinner.setValue(Integer.parseInt(settings.getOrDefault("membership.validity_days", "30")));
        expirationWarningSpinner.setValue(Integer.parseInt(settings.getOrDefault("membership.warning_days", "7")));
        autoRenewalCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("membership.auto_renewal", "false")));
        sendEmailNotificationsCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("notifications.email_enabled", "true")));
        sendSMSNotificationsCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("notifications.sms_enabled", "false")));
        
        // Payment Settings
        monthlyFeeField.setText(settings.getOrDefault("payment.monthly_fee", "500"));
        quarterlyFeeField.setText(settings.getOrDefault("payment.quarterly_fee", "1400"));
        biannualFeeField.setText(settings.getOrDefault("payment.biannual_fee", "2700"));
        annualFeeField.setText(settings.getOrDefault("payment.annual_fee", "5000"));
        currencyField.setText(settings.getOrDefault("payment.currency", "EGP"));
        lateFeePercentageSpinner.setValue(Double.parseDouble(settings.getOrDefault("payment.late_fee_percent", "5.0")));
        
        // Security Settings
        sessionTimeoutSpinner.setValue(Integer.parseInt(settings.getOrDefault("security.session_timeout", "30")));
        passwordMinLengthSpinner.setValue(Integer.parseInt(settings.getOrDefault("security.password_min_length", "8")));
        requirePasswordChangeCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("security.require_password_change", "false")));
        passwordChangeIntervalSpinner.setValue(Integer.parseInt(settings.getOrDefault("security.password_change_days", "90")));
        enableAuditLoggingCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("security.audit_logging", "true")));
        
        // Email Configuration Settings
        senderEmailField.setText(settings.getOrDefault("email.sender_address", ""));
        smtpHostField.setText(settings.getOrDefault("email.smtp_host", "smtp.gmail.com"));
        smtpPortSpinner.setValue(Integer.parseInt(settings.getOrDefault("email.smtp_port", "587")));
        smtpUsernameField.setText(settings.getOrDefault("email.smtp_username", ""));
        smtpPasswordField.setText(settings.getOrDefault("email.smtp_password", ""));
        smtpTLSCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("email.smtp_tls", "true")));
        
        // Appearance Settings
        themeComboBox.setSelectedItem(settings.getOrDefault("appearance.theme", "Dark Theme"));
        languageComboBox.setSelectedItem(settings.getOrDefault("appearance.language", "English"));
        dateFormatComboBox.setSelectedItem(settings.getOrDefault("appearance.date_format", "DD/MM/YYYY"));
        timeFormatComboBox.setSelectedItem(settings.getOrDefault("appearance.time_format", "24-hour"));
        
        // Backup Settings
        autoBackupCheckBox.setSelected(Boolean.parseBoolean(settings.getOrDefault("backup.auto_enabled", "true")));
        backupFrequencyComboBox.setSelectedItem(settings.getOrDefault("backup.frequency", "Daily"));
        backupPathField.setText(settings.getOrDefault("backup.path", "./backups"));
        backupRetentionSpinner.setValue(Integer.parseInt(settings.getOrDefault("backup.retention_days", "30")));
    }
    
    private void saveSettings() {
        try {
            // General Settings
            settingsDAO.set("gym.name", gymNameField.getText());
            settingsDAO.set("gym.email", gymEmailField.getText());
            settingsDAO.set("gym.phone", gymPhoneField.getText());
            settingsDAO.set("gym.address", gymAddressArea.getText());
            
            // Business Settings
            settingsDAO.set("membership.validity_days", membershipValiditySpinner.getValue().toString());
            settingsDAO.set("membership.warning_days", expirationWarningSpinner.getValue().toString());
            settingsDAO.set("membership.auto_renewal", String.valueOf(autoRenewalCheckBox.isSelected()));
            settingsDAO.set("notifications.email_enabled", String.valueOf(sendEmailNotificationsCheckBox.isSelected()));
            settingsDAO.set("notifications.sms_enabled", String.valueOf(sendSMSNotificationsCheckBox.isSelected()));
            
            // Payment Settings
            settingsDAO.set("payment.monthly_fee", monthlyFeeField.getText());
            settingsDAO.set("payment.quarterly_fee", quarterlyFeeField.getText());
            settingsDAO.set("payment.biannual_fee", biannualFeeField.getText());
            settingsDAO.set("payment.annual_fee", annualFeeField.getText());
            settingsDAO.set("payment.currency", currencyField.getText());
            settingsDAO.set("payment.late_fee_percent", lateFeePercentageSpinner.getValue().toString());
            
            // Security Settings
            settingsDAO.set("security.session_timeout", sessionTimeoutSpinner.getValue().toString());
            settingsDAO.set("security.password_min_length", passwordMinLengthSpinner.getValue().toString());
            settingsDAO.set("security.require_password_change", String.valueOf(requirePasswordChangeCheckBox.isSelected()));
            settingsDAO.set("security.password_change_days", passwordChangeIntervalSpinner.getValue().toString());
            settingsDAO.set("security.audit_logging", String.valueOf(enableAuditLoggingCheckBox.isSelected()));
            
            // Email Configuration Settings - Smart handling of sender email
            String newSenderEmail = senderEmailField.getText().trim();
            String existingSenderEmail = settingsDAO.get("email.sender_address");
            
            if (existingSenderEmail != null && !existingSenderEmail.isEmpty()) {
                // Sender email EXISTS - Update only the email field without touching other settings
                if (!newSenderEmail.equals(existingSenderEmail)) {
                    settingsDAO.set("email.sender_address", newSenderEmail);
                }
            } else {
                // Sender email DOES NOT exist - Add new email while keeping all other data
                if (!newSenderEmail.isEmpty()) {
                    settingsDAO.set("email.sender_address", newSenderEmail);
                }
            }
            
            // Save other email settings normally
            settingsDAO.set("email.smtp_host", smtpHostField.getText());
            settingsDAO.set("email.smtp_port", smtpPortSpinner.getValue().toString());
            settingsDAO.set("email.smtp_username", smtpUsernameField.getText());
            settingsDAO.set("email.smtp_password", new String(smtpPasswordField.getPassword()));
            settingsDAO.set("email.smtp_tls", String.valueOf(smtpTLSCheckBox.isSelected()));
            
            // Appearance Settings
            settingsDAO.set("appearance.theme", (String) themeComboBox.getSelectedItem());
            settingsDAO.set("appearance.language", (String) languageComboBox.getSelectedItem());
            settingsDAO.set("appearance.date_format", (String) dateFormatComboBox.getSelectedItem());
            settingsDAO.set("appearance.time_format", (String) timeFormatComboBox.getSelectedItem());
            
            // Backup Settings
            settingsDAO.set("backup.auto_enabled", String.valueOf(autoBackupCheckBox.isSelected()));
            settingsDAO.set("backup.frequency", (String) backupFrequencyComboBox.getSelectedItem());
            settingsDAO.set("backup.path", backupPathField.getText());
            settingsDAO.set("backup.retention_days", backupRetentionSpinner.getValue().toString());
            
            // Log audit (with null check for session)
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                auditService.logSettingsChange(
                    currentUser.getId(),
                    "System Settings Updated",
                    "127.0.0.1"
                );
            }
            
            JOptionPane.showMessageDialog(this,
                "System settings saved successfully!\n\nSome changes may require application restart to take effect.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving settings: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetToDefaults() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset all settings to default values?\nThis action cannot be undone.",
            "Reset to Defaults",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            // Reset to default values
            gymNameField.setText("My Gym");
            gymEmailField.setText("info@mygym.com");
            gymPhoneField.setText("+20 100 000 0000");
            gymAddressArea.setText("123 Main Street, Cairo, Egypt");
            
            membershipValiditySpinner.setValue(30);
            expirationWarningSpinner.setValue(7);
            autoRenewalCheckBox.setSelected(false);
            sendEmailNotificationsCheckBox.setSelected(true);
            sendSMSNotificationsCheckBox.setSelected(false);
            
            monthlyFeeField.setText("500");
            quarterlyFeeField.setText("1400");
            biannualFeeField.setText("2700");
            annualFeeField.setText("5000");
            currencyField.setText("EGP");
            lateFeePercentageSpinner.setValue(5.0);
            
            sessionTimeoutSpinner.setValue(30);
            passwordMinLengthSpinner.setValue(8);
            requirePasswordChangeCheckBox.setSelected(false);
            passwordChangeIntervalSpinner.setValue(90);
            enableAuditLoggingCheckBox.setSelected(true);
            
            senderEmailField.setText("");
            smtpHostField.setText("smtp.gmail.com");
            smtpPortSpinner.setValue(587);
            smtpUsernameField.setText("");
            smtpPasswordField.setText("");
            smtpTLSCheckBox.setSelected(true);
            
            themeComboBox.setSelectedItem("Dark Theme");
            languageComboBox.setSelectedItem("English");
            dateFormatComboBox.setSelectedItem("DD/MM/YYYY");
            timeFormatComboBox.setSelectedItem("24-hour");
            
            autoBackupCheckBox.setSelected(true);
            backupFrequencyComboBox.setSelectedItem("Daily");
            backupPathField.setText("./backups");
            backupRetentionSpinner.setValue(30);
            
            JOptionPane.showMessageDialog(this,
                "Settings have been reset to default values.\nClick 'Save All Settings' to apply changes.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void testEmailSettings() {
        // Get email configuration from fields
        String senderEmail = senderEmailField.getText().trim();
        String smtpHost = smtpHostField.getText().trim();
        int smtpPort = (Integer) smtpPortSpinner.getValue();
        String smtpUsername = smtpUsernameField.getText().trim();
        String smtpPassword = new String(smtpPasswordField.getPassword());
        boolean useTLS = smtpTLSCheckBox.isSelected();
        
        // Validate fields
        if (senderEmail.isEmpty() || !senderEmail.contains("@")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid sender email address.",
                "Invalid Email",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (smtpHost.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter SMTP host.",
                "Invalid Configuration",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (smtpUsername.isEmpty() || smtpPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter SMTP username and password.",
                "Invalid Configuration",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show progress dialog
        JOptionPane.showMessageDialog(this,
            "Sending test email to: flowmoner@gmail.com\n" +
            "From: " + senderEmail + "\n" +
            "SMTP Host: " + smtpHost + ":" + smtpPort + "\n\n" +
            "Please wait...",
            "Sending Email",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Send test email in background thread
        new Thread(() -> {
            try {
                sendTestEmail(senderEmail, smtpHost, smtpPort, smtpUsername, smtpPassword, useTLS);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "✓ Test email sent successfully!\n\n" +
                        "Email sent to: flowmoner@gmail.com\n" +
                        "Subject: Gym Management System - Test Email\n\n" +
                        "Please check your inbox (and spam folder).",
                        "Email Sent",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "✗ Failed to send test email.\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Please check your SMTP configuration:\n" +
                        "- Sender email: " + senderEmail + "\n" +
                        "- SMTP host: " + smtpHost + ":" + smtpPort + "\n" +
                        "- Username: " + smtpUsername + "\n" +
                        "- TLS: " + (useTLS ? "Enabled" : "Disabled") + "\n\n" +
                        "For Gmail, you need to:\n" +
                        "1. Enable 2-factor authentication\n" +
                        "2. Generate an App Password\n" +
                        "3. Use the App Password (not your regular password)",
                        "Email Failed",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void sendTestEmail(String from, String host, int port, String username, 
                               String password, boolean useTLS) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", useTLS ? "true" : "false");
        
        javax.mail.Session session = javax.mail.Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(username, password);
                }
            });
        
        javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
        message.setFrom(new javax.mail.internet.InternetAddress(from));
        message.setRecipients(javax.mail.Message.RecipientType.TO,
            javax.mail.internet.InternetAddress.parse("flowmoner@gmail.com"));
        message.setSubject("Gym Management System - Test Email");
        
        String htmlBody = "<html><body style='font-family: Arial, sans-serif;'>" +
            "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px; text-align: center;'>" +
            "<h1 style='color: white; margin: 0;'>🏋️ Gym Management System</h1>" +
            "<p style='color: white; font-size: 18px; margin-top: 10px;'>Email System Test</p>" +
            "</div>" +
            "<div style='padding: 30px; background-color: #f8f9fa;'>" +
            "<h2 style='color: #333;'>✓ System Working!</h2>" +
            "<p style='color: #666; font-size: 16px; line-height: 1.6;'>" +
            "Your email configuration is working correctly. The Gym Management System " +
            "can now send notifications and reports via email." +
            "</p>" +
            "<div style='background: white; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0;'>" +
            "<strong style='color: #333;'>Configuration Details:</strong><br>" +
            "<span style='color: #666;'>Sender: " + from + "</span><br>" +
            "<span style='color: #666;'>SMTP Server: " + host + ":" + port + "</span><br>" +
            "<span style='color: #666;'>TLS Encryption: " + (useTLS ? "Enabled" : "Disabled") + "</span><br>" +
            "<span style='color: #666;'>Test Date: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</span>" +
            "</div>" +
            "<p style='color: #999; font-size: 14px; margin-top: 30px;'>" +
            "This is an automated test email from the Gym Management System.<br>" +
            "If you received this, your email configuration is working perfectly!" +
            "</p>" +
            "</div>" +
            "</body></html>";
        
        message.setContent(htmlBody, "text/html; charset=utf-8");
        
        javax.mail.Transport.send(message);
    }
    
    private void browseBackupPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Backup Directory");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            backupPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    /**
     * Adds Enter key navigation to text fields (acts like Tab key).
     */
    private void addEnterKeyNavigation(JComponent component) {
        component.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    component.transferFocus();
                }
            }
        });
    }
}

/**
 * Custom rounded border for modern UI elements
 */
class RoundedBorder extends AbstractBorder {
    private final Color color;
    private final int thickness;
    private final int radius;
    
    public RoundedBorder(Color color, int thickness, int radius) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2d.dispose();
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius/2, radius/2, radius/2, radius/2);
    }
    
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = radius/2;
        return insets;
    }
}
