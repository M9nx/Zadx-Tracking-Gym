package app.views.coach;

import app.model.User;
import app.util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Coach Dashboard - Main interface for Coach role.
 * 
 * Features:
 * - View assigned members only
 * - Update training progress
 * - Limited access (read-only for most data)
 * 
 * Navigation Options:
 * - My Members (view only)
 * - Training Progress
 * - Logout
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class CoachDashboard extends JFrame {
    
    private final User currentUser;
    private JPanel mainContentPanel;
    
    public CoachDashboard(User user) {
        this.currentUser = user;
        
        initComponents();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        setTitle("Gym Management System - Coach Dashboard");
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
                g2d.setColor(new Color(50, 50, 50, 180));
                g2d.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 15, 15);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(5, 5, getWidth()-11, getHeight()-11, 15, 15);
                g2d.dispose();
            }
        };
        mainContentPanel.setOpaque(false);
        mainContentPanel.setLayout(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("<html><h1>Welcome, " + currentUser.getFirstName() + "!</h1><p>Coach Panel</p></html>");
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
                g2d.setColor(new Color(50, 50, 50, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
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
        
        JLabel titleLabel = new JLabel("COACH PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(20, yPos, 210, 30);
        sidebar.add(titleLabel);
        yPos += 50;
        
        JButton myMembersBtn = createGlassButton("My Members");
        myMembersBtn.setBounds(20, yPos, 210, 45);
        myMembersBtn.addActionListener(e -> openMyMembers());
        sidebar.add(myMembersBtn);
        yPos += 55;
        
        JButton progressBtn = createGlassButton("Training Progress");
        progressBtn.setBounds(20, yPos, 210, 45);
        progressBtn.addActionListener(e -> openTrainingProgress());
        sidebar.add(progressBtn);
        yPos += 55;
        
        JButton profileBtn = createGlassButton("My Profile");
        profileBtn.setBounds(20, yPos, 210, 45);
        profileBtn.addActionListener(e -> openProfile());
        sidebar.add(profileBtn);
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
    
    private void openMyMembers() {
        // Coach can only see their assigned members
        mainContentPanel.removeAll();
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        app.views.coach.CoachMembersView panel = new app.views.coach.CoachMembersView(currentUser);
        mainContentPanel.add(panel, BorderLayout.CENTER);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void openTrainingProgress() {
        mainContentPanel.removeAll();
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        app.views.coach.TrainingProgressView panel = new app.views.coach.TrainingProgressView(currentUser);
        mainContentPanel.add(panel, BorderLayout.CENTER);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void openProfile() {
        mainContentPanel.removeAll();
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        app.views.coach.CoachProfileView panel = new app.views.coach.CoachProfileView(currentUser);
        mainContentPanel.add(panel, BorderLayout.CENTER);
        
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
