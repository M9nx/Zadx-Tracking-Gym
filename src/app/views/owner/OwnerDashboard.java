package app.views.owner;

import app.model.User;
import app.util.SessionManager;
import java.awt.*;
import javax.swing.*;

/**
 * Owner Dashboard - Main interface for Owner role.
 * 
 * Features:
 * - Sidebar navigation with role-specific buttons
 * - Main content area for displaying views
 * - Branch selector (can view all branches)
 * - System-wide statistics
 * - Full access to all management features
 * 
 * Navigation Options:
 * - Manage Branches
 * - Manage Admins
 * - Manage Coaches
 * - Manage Members (all branches)
 * - System Statistics
 * - System Settings
 * - Audit Logs
 * - Logout
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class OwnerDashboard extends JFrame {
    
    private final User currentUser;
    private JPanel mainContentPanel;
    private JLabel welcomeLabel;
    
    public OwnerDashboard(User user) {
        this.currentUser = user;
        
        initComponents();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        setTitle("Gym Management System - Owner Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Main panel with background image
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    java.awt.Image img = new javax.swing.ImageIcon(getClass().getResource("/Images/ambitious-studio-rick-barrett-1RNQ11ZODJM-unsplash.jpg")).getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    setBackground(new Color(0, 0, 0));
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        
        // Main content area with blur gray glass effect
        mainContentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Blur gray glass effect
                g2d.setColor(new Color(50, 50, 50, 180));
                g2d.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 15, 15);
                // Subtle border
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(5, 5, getWidth()-11, getHeight()-11, 15, 15);
                g2d.dispose();
            }
        };
        mainContentPanel.setOpaque(false);
        mainContentPanel.setLayout(new BorderLayout());
        
        welcomeLabel = new JLabel("<html><h1>Welcome, " + currentUser.getFirstName() + "!</h1><p>Select an option from the sidebar</p></html>");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainContentPanel.add(welcomeLabel, BorderLayout.CENTER);
        
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Blur gray glass effect
                g2d.setColor(new Color(50, 50, 50, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                // Subtle border
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);
                g2d.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setLayout(null);
        
        int yPos = 20;
        
        // Title
        JLabel titleLabel = new JLabel("OWNER PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(20, yPos, 210, 30);
        sidebar.add(titleLabel);
        yPos += 50;
        
        // Navigation buttons with glass effect
        JButton branchesBtn = createGlassButton("Manage Branches");
        branchesBtn.setBounds(20, yPos, 210, 45);
        branchesBtn.addActionListener(e -> openManageBranches());
        sidebar.add(branchesBtn);
        yPos += 55;
        
        JButton adminsBtn = createGlassButton("Manage Admins");
        adminsBtn.setBounds(20, yPos, 210, 45);
        adminsBtn.addActionListener(e -> openManageAdmins());
        sidebar.add(adminsBtn);
        yPos += 55;
        
        JButton coachesBtn = createGlassButton("Manage Coaches");
        coachesBtn.setBounds(20, yPos, 210, 45);
        coachesBtn.addActionListener(e -> openManageCoaches());
        sidebar.add(coachesBtn);
        yPos += 55;
        
        JButton membersBtn = createGlassButton("Manage Members");
        membersBtn.setBounds(20, yPos, 210, 45);
        membersBtn.addActionListener(e -> openManageMembers());
        sidebar.add(membersBtn);
        yPos += 55;
        
        JButton statisticsBtn = createGlassButton("Statistics");
        statisticsBtn.setBounds(20, yPos, 210, 45);
        statisticsBtn.addActionListener(e -> openStatistics());
        sidebar.add(statisticsBtn);
        yPos += 55;
        
        JButton settingsBtn = createGlassButton("System Settings");
        settingsBtn.setBounds(20, yPos, 210, 45);
        settingsBtn.addActionListener(e -> openSystemSettings());
        sidebar.add(settingsBtn);
        yPos += 55;
        
        JButton auditBtn = createGlassButton("Audit Logs");
        auditBtn.setBounds(20, yPos, 210, 45);
        auditBtn.addActionListener(e -> openAuditLogs());
        sidebar.add(auditBtn);
        yPos += 70;
        
        JButton logoutBtn = createGlassButton("Logout");
        logoutBtn.setForeground(new Color(255, 100, 100));
        logoutBtn.setBounds(20, yPos, 210, 45);
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
        
        return sidebar;
    }
    
    private JButton createGlassButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(50, 50, 50));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(90, 90, 90));
                } else {
                    g2d.setColor(new Color(70, 70, 70));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        return button;
    }
    
    private void openManageBranches() {
        try {
            mainContentPanel.removeAll();
            app.views.shared.ManageBranchesView panel = new app.views.shared.ManageBranchesView();
            mainContentPanel.add(panel, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Manage Branches feature coming soon!\n" + e.getMessage(),
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void openManageAdmins() {
        try {
            mainContentPanel.removeAll();
            app.views.shared.ManageUsersView panel = new app.views.shared.ManageUsersView(app.model.UserRole.ADMIN);
            mainContentPanel.add(panel, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Manage Admins feature coming soon!\n" + e.getMessage(),
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void openManageCoaches() {
        try {
            mainContentPanel.removeAll();
            app.views.shared.ManageUsersView panel = new app.views.shared.ManageUsersView(app.model.UserRole.COACH);
            mainContentPanel.add(panel, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Manage Coaches feature coming soon!\n" + e.getMessage(),
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void openManageMembers() {
        try {
            mainContentPanel.removeAll();
            app.views.shared.ManageMembersView panel = new app.views.shared.ManageMembersView();
            mainContentPanel.add(panel, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Manage Members feature coming soon!\n" + e.getMessage(),
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void openStatistics() {
        try {
            mainContentPanel.removeAll();
            app.views.shared.StatisticsView panel = new app.views.shared.StatisticsView();
            mainContentPanel.add(panel, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openSystemSettings() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new SystemSettingsView(), BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void openAuditLogs() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new app.views.AuditLogsView(), BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().endSession();
            dispose();
            try {
                Class<?> loginClass = Class.forName("LOGIN");
                javax.swing.JFrame loginFrame = (javax.swing.JFrame) loginClass.getDeclaredConstructor().newInstance();
                loginFrame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading login screen: " + ex.getMessage());
            }
        }
    }
}
