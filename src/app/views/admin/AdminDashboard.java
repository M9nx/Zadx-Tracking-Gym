package app.views.admin;

import app.model.User;
import app.util.SessionManager;
import java.awt.*;
import javax.swing.*;

/**
 * Admin Dashboard - Main interface for Admin role.
 * 
 * Features:
 * - Branch-restricted access
 * - Member management for assigned branch
 * - Coach management for assigned branch
 * - Branch-specific reports
 * 
 * Navigation Options:
 * - Manage Members (branch only)
 * - Manage Coaches (branch only)
 * - Branch Reports
 * - Logout
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class AdminDashboard extends JFrame {
    
    private final User currentUser;
    private JPanel mainContentPanel;
    
    public AdminDashboard(User user) {
        this.currentUser = user;
        
        initComponents();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        setTitle("Gym Management System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
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
        
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        
        mainContentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(45, 45, 55, 200),
                    0, getHeight(), new Color(35, 35, 45, 180)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 20, 20);
                
                // Subtle purple border
                g2d.setColor(new Color(139, 92, 246, 100));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(5, 5, getWidth()-11, getHeight()-11, 20, 20);
                
                g2d.dispose();
            }
        };
        mainContentPanel.setOpaque(false);
        mainContentPanel.setLayout(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("<html><div style='text-align:center;'><h1 style='color:#E6E6F0;margin:20px;'>Welcome, " + currentUser.getFirstName() + "! ⚡</h1><p style='color:#B0B0C0;font-size:14px;'>Branch Admin Control Panel</p></div></html>");
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
                
                // Glassmorphism blur effect with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(40, 40, 48, 220),
                    0, getHeight(), new Color(30, 30, 38, 200)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
                
                // Subtle border
                g2d.setColor(new Color(139, 92, 246, 100));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                
                g2d.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setLayout(null);
        
        int yPos = 30;
        
        // Title with icon
        JLabel titleLabel = new JLabel("ADMIN PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(230, 230, 240));
        titleLabel.setBounds(20, yPos, 210, 35);
        sidebar.add(titleLabel);
        yPos += 60;
        
        JButton membersBtn = createGlassButton("Manage Members");
        membersBtn.setBounds(20, yPos, 210, 48);
        membersBtn.addActionListener(e -> openManageMembers());
        sidebar.add(membersBtn);
        yPos += 58;
        
        JButton coachesBtn = createGlassButton("Manage Coaches");
        coachesBtn.setBounds(20, yPos, 210, 48);
        coachesBtn.addActionListener(e -> openManageCoaches());
        sidebar.add(coachesBtn);
        yPos += 58;
        
        JButton reportsBtn = createGlassButton("Branch Reports");
        reportsBtn.setBounds(20, yPos, 210, 48);
        reportsBtn.addActionListener(e -> openBranchReports());
        sidebar.add(reportsBtn);
        yPos += 58;
        
        JButton profileBtn = createGlassButton("My Profile");
        profileBtn.setBounds(20, yPos, 210, 48);
        profileBtn.addActionListener(e -> openProfile());
        sidebar.add(profileBtn);
        yPos += 80;
        
        JButton logoutBtn = createGlassButton("Logout");
        logoutBtn.setForeground(new Color(255, 120, 120));
        logoutBtn.setBounds(20, yPos, 210, 48);
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
                    g2d.setColor(new Color(139, 92, 246, 150));
                } else if (getModel().isRollover()) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(139, 92, 246, 120),
                        0, getHeight(), new Color(80, 50, 150, 100)
                    );
                    g2d.setPaint(gradient);
                } else {
                    g2d.setColor(new Color(60, 60, 75, 140));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border
                g2d.setColor(new Color(139, 92, 246, getModel().isRollover() ? 150 : 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        button.setForeground(new Color(230, 230, 240));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        return button;
    }
    
    private void openManageMembers() {
        if (currentUser.getBranchId() != null) {
            try {
                mainContentPanel.removeAll();
                mainContentPanel.revalidate();
                mainContentPanel.repaint();
                
                app.views.shared.ManageMembersView panel = new app.views.shared.ManageMembersView(currentUser.getBranchId());
                mainContentPanel.add(panel, BorderLayout.CENTER);
                mainContentPanel.revalidate();
                mainContentPanel.repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Manage Members feature coming soon!\n" + e.getMessage(),
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No branch assigned to your account.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openManageCoaches() {
        if (currentUser.getBranchId() != null) {
            try {
                mainContentPanel.removeAll();
                mainContentPanel.revalidate();
                mainContentPanel.repaint();
                
                ManageCoachesView panel = new ManageCoachesView(currentUser.getBranchId());
                mainContentPanel.add(panel, BorderLayout.CENTER);
                mainContentPanel.revalidate();
                mainContentPanel.repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading Manage Coaches: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No branch assigned to your account.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openBranchReports() {
        try {
            mainContentPanel.removeAll();
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
            
            BranchReportsView reportsView = new BranchReportsView();
            mainContentPanel.add(reportsView, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            System.err.println("Error opening Branch Reports: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error opening Branch Reports:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openProfile() {
        try {
            mainContentPanel.removeAll();
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
            
            AdminProfileView panel = new AdminProfileView(currentUser);
            mainContentPanel.add(panel, BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
