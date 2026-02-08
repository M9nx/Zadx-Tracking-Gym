import app.service.UserService;
import app.util.EmailService;
import app.util.PasswordUtil;
import java.awt.*;
import javax.swing.*;

/**
 * Password Reset Page - Email-based password reset for all user roles
 * 
 * Features:
 * - Email-based password reset (SMTP)
 * - Works for Owner, Admin, and Coach
 * - Blur/glass UI design
 * - Back to login button
 * - Generates random password and emails to user
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class PasswordResetPage extends JFrame {
    
    private final UserService userService;
    private final EmailService emailService;
    
    private JTextField usernameField;
    private JTextField emailField;
    private JButton resetButton;
    private JButton backButton;
    
    public PasswordResetPage() {
        this.userService = new UserService();
        this.emailService = new EmailService();
        
        initComponents();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Password Reset - Gym Management System");
        
        // Main background panel with image
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    Image img = new ImageIcon(getClass().getResource("/Images/ambitious-studio-rick-barrett-1RNQ11ZODJM-unsplash.jpg")).getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    setBackground(new Color(0, 0, 0));
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        
        // Glass panel container
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Blur gray glass effect
                g2d.setColor(new Color(50, 50, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // White border
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                g2d.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setLayout(new GridBagLayout());
        glassPanel.setPreferredSize(new Dimension(500, 450));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        
        // Title
        JLabel titleLabel = new JLabel("Password Reset");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        glassPanel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Enter your username and email to reset password");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 20, 20);
        glassPanel.add(subtitleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 20, 15, 20);
        
        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        glassPanel.add(usernameLabel, gbc);
        
        // Username field
        usernameField = createStyledTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        glassPanel.add(usernameField, gbc);
        
        // Email label
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        glassPanel.add(emailLabel, gbc);
        
        // Email field
        emailField = createStyledTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        glassPanel.add(emailField, gbc);
        
        // Info label
        JLabel infoLabel = new JLabel("<html><center>A new password will be generated and sent to your email</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(255, 200, 100));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 20, 20);
        glassPanel.add(infoLabel, gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        // Reset button
        resetButton = createGlassButton("Reset Password", new Color(50, 150, 50), new Color(70, 170, 70));
        resetButton.setPreferredSize(new Dimension(180, 45));
        resetButton.addActionListener(e -> handlePasswordReset());
        buttonsPanel.add(resetButton);
        
        // Back button
        backButton = createGlassButton("Back to Login", new Color(100, 100, 100), new Color(120, 120, 120));
        backButton.setPreferredSize(new Dimension(180, 45));
        backButton.addActionListener(e -> backToLogin());
        buttonsPanel.add(backButton);
        
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 15, 20);
        glassPanel.add(buttonsPanel, gbc);
        
        backgroundPanel.add(glassPanel);
        setContentPane(backgroundPanel);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(new Color(70, 70, 70));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setOpaque(false);
        return field;
    }
    
    private JButton createGlassButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color currentColor = hover ? hoverColor : baseColor;
                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.putClientProperty("hover", true);
                button.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.putClientProperty("hover", false);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void handlePasswordReset() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        
        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your username.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your email address.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        // Email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        // Disable button during processing
        resetButton.setEnabled(false);
        resetButton.setText("Processing...");
        
        // Process in background to keep UI responsive
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    // Reset password via service
                    String result = userService.resetPasswordByEmail(username, email, "127.0.0.1");
                    return result;
                } catch (Exception e) {
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    
                    if (result != null && result.startsWith("SUCCESS:")) {
                        String newPassword = result.substring(8);
                        JOptionPane.showMessageDialog(PasswordResetPage.this,
                            "Password reset successful!\n\n" +
                            "Your new password is: " + newPassword + "\n\n" +
                            "An email has been sent to: " + email + "\n\n" +
                            "Please check your inbox and login with the new password.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Clear fields
                        usernameField.setText("");
                        emailField.setText("");
                        
                        // Return to login
                        backToLogin();
                    } else if (result != null && result.startsWith("ERROR:")) {
                        String errorMessage = result.substring(6);
                        JOptionPane.showMessageDialog(PasswordResetPage.this,
                            errorMessage,
                            "Reset Failed",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PasswordResetPage.this,
                            "Password reset failed. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PasswordResetPage.this,
                        "Error resetting password: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    resetButton.setEnabled(true);
                    resetButton.setText("Reset Password");
                }
            }
        };
        
        worker.execute();
    }
    
    private void backToLogin() {
        LOGIN loginFrame = new LOGIN();
        loginFrame.setVisible(true);
        this.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new PasswordResetPage().setVisible(true);
        });
    }
}
