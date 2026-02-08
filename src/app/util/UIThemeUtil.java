package app.util;

import java.awt.*;
import javax.swing.*;

/**
 * UIThemeUtil - Centralized UI theme utilities
 * Provides consistent glass-morphism styling across all windows
 */
public class UIThemeUtil {
    
    // Color scheme
    public static final Color BACKGROUND_DARK = new Color(0, 0, 0);
    public static final Color GLASS_PANEL = new Color(50, 50, 50, 200);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 50);
    public static final Color TEXT_WHITE = new Color(255, 255, 255);
    public static final Color TEXT_GRAY = new Color(200, 200, 200);
    public static final Color BUTTON_GRAY = new Color(70, 70, 70);
    public static final Color BUTTON_HOVER = new Color(90, 90, 90);
    public static final Color BUTTON_PRESSED = new Color(50, 50, 50);
    
    // Role-specific button colors
    public static final Color BUTTON_OWNER = new Color(41, 128, 185);      // Blue
    public static final Color BUTTON_OWNER_HOVER = new Color(52, 152, 219);
    public static final Color BUTTON_ADMIN = new Color(39, 174, 96);       // Green
    public static final Color BUTTON_ADMIN_HOVER = new Color(46, 204, 113);
    public static final Color BUTTON_COACH = new Color(230, 126, 34);      // Orange
    public static final Color BUTTON_COACH_HOVER = new Color(243, 156, 18);
    
    // Additional UI colors (for newer views)
    public static final Color OWNER_PRIMARY = new Color(45, 52, 54);       // Dark gray
    public static final Color OWNER_ACCENT = new Color(41, 128, 185);      // Blue (same as BUTTON_OWNER)
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);      // Green
    public static final Color ERROR_COLOR = new Color(192, 57, 43);        // Red
    
    // Dimensions
    public static final int PANEL_CORNER_RADIUS = 25;
    public static final int BUTTON_CORNER_RADIUS = 15;
    
    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 72);
    public static final Font FONT_LARGE = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 16);
    
    // Additional font aliases (for newer views)
    public static final Font HEADER_FONT = FONT_LARGE;
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font INPUT_FONT = FONT_NORMAL;
    
    /**
     * Create a glass-morphism panel
     */
    public static JPanel createGlassPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glass effect
                g2d.setColor(GLASS_PANEL);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 
                                PANEL_CORNER_RADIUS, PANEL_CORNER_RADIUS);
                
                // Border
                g2d.setColor(GLASS_BORDER);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2,
                                PANEL_CORNER_RADIUS, PANEL_CORNER_RADIUS);
                
                g2d.dispose();
            }
        };
    }
    
    /**
     * Create a styled button with glass effect
     */
    public static JButton createStyledButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color btnColor;
                if (getModel().isPressed()) {
                    btnColor = BUTTON_PRESSED;
                } else if (getModel().isRollover()) {
                    btnColor = hoverColor;
                } else {
                    btnColor = baseColor;
                }
                
                g2d.setColor(btnColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(),
                                BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setForeground(TEXT_WHITE);
        button.setFont(FONT_BUTTON);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * Create gray button (default style)
     */
    public static JButton createGrayButton(String text) {
        return createStyledButton(text, BUTTON_GRAY, BUTTON_HOVER);
    }
    
    /**
     * Create owner button (blue)
     */
    public static JButton createOwnerButton(String text) {
        return createStyledButton(text, BUTTON_OWNER, BUTTON_OWNER_HOVER);
    }
    
    /**
     * Create admin button (green)
     */
    public static JButton createAdminButton(String text) {
        return createStyledButton(text, BUTTON_ADMIN, BUTTON_ADMIN_HOVER);
    }
    
    /**
     * Create coach button (orange)
     */
    public static JButton createCoachButton(String text) {
        return createStyledButton(text, BUTTON_COACH, BUTTON_COACH_HOVER);
    }
    
    /**
     * Create styled text field with rounded corners
     */
    public static JTextField createStyledTextField() {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        textField.setOpaque(false);
        textField.setBackground(new Color(255, 255, 255));
        textField.setForeground(new Color(0, 0, 0));
        textField.setFont(FONT_NORMAL);
        textField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        return textField;
    }
    
    /**
     * Create styled password field
     */
    public static JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        passwordField.setOpaque(false);
        passwordField.setBackground(new Color(255, 255, 255));
        passwordField.setForeground(new Color(0, 0, 0));
        passwordField.setFont(FONT_NORMAL);
        passwordField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        return passwordField;
    }
    
    /**
     * Create styled label
     */
    public static JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_WHITE);
        label.setFont(font);
        return label;
    }
    
    /**
     * Show styled message dialog
     */
    public static void showMessage(Component parent, String message, String title, int messageType) {
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
    
    /**
     * Show error message
     */
    public static void showError(Component parent, String message) {
        showMessage(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show success message
     */
    public static void showSuccess(Component parent, String message) {
        showMessage(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Set window to fullscreen and apply background
     */
    public static void setupWindow(JFrame frame, String backgroundImagePath) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        // Add component listener for responsive resizing
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                frame.revalidate();
                frame.repaint();
            }
        });
    }
}
