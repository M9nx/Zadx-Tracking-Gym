package app.views;

import app.service.AuthenticationService;
import app.service.AuthenticationService.LoginResult;
import app.model.User;
import app.model.UserRole;
import app.util.UIThemeUtil;
import app.views.owner.OwnerDashboard;
import app.views.admin.AdminDashboard;
import app.views.coach.CoachDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;

/**
 * Login View - Main entry point for the Gym Management System.
 * 
 * Features:
 * - Full-screen glass-morphism design with background image
 * - Username and password fields
 * - Login button with authentication
 * - Forgot password link (owner only)
 * - Role-based dashboard navigation
 * - IP address tracking for audit logging
 * 
 * Design:
 * - Dark theme with blur/glass panels (alpha 200)
 * - 25px rounded corners
 * - White text on dark background
 * - Background image: Images/background.jpg
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class LoginView extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel forgotPasswordLabel;
    private JLabel statusLabel;
    
    private final AuthenticationService authService;
    private String clientIpAddress;
    
    /**
     * Constructs the LoginView with full-screen display and glass-morphism theme.
     */
    public LoginView() {
        this.authService = new AuthenticationService();
        this.clientIpAddress = getClientIpAddress();
        
        initComponents();
        setupEventListeners();
        
        // Center on screen
        setLocationRelativeTo(null);
        
        // Make visible
        setVisible(true);
    }
    
    /**
     * Initializes all UI components with glass-morphism styling.
     */
    private void initComponents() {
        // Frame settings
        setTitle("Gym Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setUndecorated(false); // Keep window decorations
        
        // Main panel with background image
        JPanel mainPanel = new JPanel() {
            private Image backgroundImage;
            
            {
                try {
                    backgroundImage = new ImageIcon(getClass().getResource("/Images/background.jpg")).getImage();
                } catch (Exception e) {
                    System.err.println("Background image not found: " + e.getMessage());
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback: Dark gradient background
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(20, 20, 30),
                        0, getHeight(), new Color(40, 40, 60)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        
        // Create glass panel for login form
        JPanel loginPanel = UIThemeUtil.createGlassPanel();
        loginPanel.setLayout(null);
        loginPanel.setBounds(0, 0, 450, 500); // Will be centered
        
        // Title label
        JLabel titleLabel = new JLabel("GYM MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(50, 40, 350, 40);
        loginPanel.add(titleLabel);
        
        // Subtitle label
        JLabel subtitleLabel = new JLabel("Login to your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBounds(50, 85, 350, 25);
        loginPanel.add(subtitleLabel);
        
        // Username label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBounds(50, 140, 350, 25);
        loginPanel.add(usernameLabel);
        
        // Username field
        usernameField = UIThemeUtil.createStyledTextField();
        usernameField.setBounds(50, 170, 350, 45);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginPanel.add(usernameField);
        
        // Password label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setBounds(50, 235, 350, 25);
        loginPanel.add(passwordLabel);
        
        // Password field
        passwordField = UIThemeUtil.createStyledPasswordField();
        passwordField.setBounds(50, 265, 350, 45);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginPanel.add(passwordField);
        
        // Login button
        loginButton = UIThemeUtil.createStyledButton("LOGIN", new Color(70, 130, 180), new Color(100, 150, 200)); // Steel blue
        loginButton.setBounds(50, 340, 350, 50);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginPanel.add(loginButton);
        
        // Forgot password label (clickable)
        forgotPasswordLabel = new JLabel("<html><u>Forgot Password?</u></html>");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(new Color(100, 150, 255));
        forgotPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        forgotPasswordLabel.setBounds(50, 405, 350, 25);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginPanel.add(forgotPasswordLabel);
        
        // Status label (for error/success messages)
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBounds(50, 435, 350, 25);
        loginPanel.add(statusLabel);
        
        // Add login panel to main panel (centered)
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerLoginPanel(mainPanel, loginPanel);
            }
        });
        
        mainPanel.add(loginPanel);
        
        // Set main panel as content pane
        setContentPane(mainPanel);
        
        // Initial centering
        centerLoginPanel(mainPanel, loginPanel);
    }
    
    /**
     * Centers the login panel on the main panel.
     */
    private void centerLoginPanel(JPanel mainPanel, JPanel loginPanel) {
        int x = (mainPanel.getWidth() - loginPanel.getWidth()) / 2;
        int y = (mainPanel.getHeight() - loginPanel.getHeight()) / 2;
        loginPanel.setBounds(x, y, loginPanel.getWidth(), loginPanel.getHeight());
    }
    
    /**
     * Sets up event listeners for login button, Enter key, and forgot password link.
     */
    private void setupEventListeners() {
        // Login button action
        loginButton.addActionListener(e -> performLogin());
        
        // Enter key in password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                } 
            }
        });
        
        // Enter key in username field (move to password)
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
        
        // Forgot password link
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setForeground(new Color(150, 200, 255));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setForeground(new Color(100, 150, 255));
            }
        });
    }
    
    /**
     * Performs login authentication when user clicks Login button or presses Enter.
     */
    private void performLogin() {
        // Clear previous status
        statusLabel.setText("");
        
        // Get credentials
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Basic validation
        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }
        
        // Disable login button during authentication
        loginButton.setEnabled(false);
        loginButton.setText("LOGGING IN...");
        
        // Perform authentication in background thread
        SwingWorker<LoginResult, Void> worker = new SwingWorker<LoginResult, Void>() {
            @Override
            protected LoginResult doInBackground() throws Exception {
                return authService.login(username, password, clientIpAddress);
            }
            
            @Override
            protected void done() {
                try {
                    LoginResult result = get();
                    
                    if (result.isSuccess()) {
                        // Login successful
                        User user = result.getUser().get();
                        showSuccess("Login successful! Welcome, " + user.getFirstName());
                        
                        // Navigate to appropriate dashboard based on role
                        navigateToDashboard(user);
                        
                    } else {
                        // Login failed
                        showError(result.getMessage());
                        passwordField.setText(""); // Clear password field
                        passwordField.requestFocus();
                    }
                    
                } catch (Exception e) {
                    showError("System error during login. Please try again.");
                    e.printStackTrace();
                    
                } finally {
                    // Re-enable login button
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Navigates to the appropriate dashboard based on user role.
     * 
     * @param user the authenticated user
     */
    private void navigateToDashboard(User user) {
        // Close login window
        dispose();
        
        // Open appropriate dashboard
        SwingUtilities.invokeLater(() -> {
            try {
                if (user.getRole() == UserRole.OWNER) {
                    new OwnerDashboard(user);
                } else if (user.getRole() == UserRole.ADMIN) {
                    new AdminDashboard(user);
                } else if (user.getRole() == UserRole.COACH) {
                    new CoachDashboard(user);
                }
            } catch (Exception e) {
                System.err.println("Error opening dashboard: " + e.getMessage());
                e.printStackTrace();
                
                // Show error and reopen login
                JOptionPane.showMessageDialog(
                    null,
                    "Error opening dashboard: " + e.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE
                );
                
                new LoginView();
            }
        });
    }
    
    /**
     * Handles forgot password request (owner only).
     */
    private void handleForgotPassword() {
        // Prompt for username
        String username = JOptionPane.showInputDialog(
            this,
            "Enter your username to reset password:\n\n" +
            "Note: Only the OWNER can use this feature.\n" +
            "A new password will be sent to m9nx11@gmail.com",
            "Forgot Password",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (username == null || username.trim().isEmpty()) {
            return; // User cancelled
        }
        
        // Show processing message
        JDialog processingDialog = new JDialog(this, "Processing...", true);
        JLabel processingLabel = new JLabel("Resetting password, please wait...");
        processingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        processingLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        processingDialog.add(processingLabel);
        processingDialog.pack();
        processingDialog.setLocationRelativeTo(this);
        
        // Process password reset in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.ownerForgotPassword(username.trim(), clientIpAddress);
            }
            
            @Override
            protected void done() {
                processingDialog.dispose();
                
                try {
                    boolean success = get();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            LoginView.this,
                            "Password reset successful!\n\n" +
                            "A new password has been sent to m9nx11@gmail.com\n" +
                            "Please check your email and login with the new password.",
                            "Password Reset Successful",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            LoginView.this,
                            "Password reset failed.\n\n" +
                            "Please ensure:\n" +
                            "- Username is correct\n" +
                            "- You are the OWNER (not admin/coach)\n" +
                            "- Your account is active",
                            "Password Reset Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        LoginView.this,
                        "System error during password reset.\n" +
                        "Please try again later or contact support.",
                        "System Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
        processingDialog.setVisible(true); // Blocks until worker completes
    }
    
    /**
     * Shows an error message in the status label.
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(255, 100, 100));
    }
    
    /**
     * Shows a success message in the status label.
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(100, 255, 150));
    }
    
    /**
     * Gets the client's IP address for audit logging.
     * 
     * @return the IP address as a string, or "unknown" if not available
     */
    private String getClientIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Main entry point for the application.
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show login view
        SwingUtilities.invokeLater(() -> new LoginView());
    }
}
