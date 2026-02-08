package app.views.shared;

import app.model.User;
import app.model.UserRole;
import app.service.UserService;
import app.util.SessionManager;
import app.util.UIThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Manage Users View - Main interface for user CRUD operations.
 * 
 * Features:
 * - JTable with user list
 * - Filter by role (Admin/Coach)
 * - Add/Edit/Delete operations
 * - Password reset functionality
 * - Glass-morphism design
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class ManageUsersView extends JPanel {
    
    private final UserService userService;
    private final SessionManager sessionManager;
    private final UserRole filterRole; // null for all roles
    
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton resetPasswordButton;
    
    /**
     * Constructor for all users view (Owner only).
     */
    public ManageUsersView() {
        this(null);
    }
    
    /**
     * Constructor for filtered view by role.
     */
    public ManageUsersView(UserRole filterRole) {
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance();
        this.filterRole = filterRole;
        
        initComponents();
        loadUsers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create glass panel wrapper
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(50, 50, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.dispose();
            }
        };
        glassPanel.setLayout(new BorderLayout(10, 10));
        glassPanel.setOpaque(false);
        glassPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);
        
        String titleText = "User Management";
        if (filterRole != null) {
            if (filterRole == UserRole.ADMIN) {
                titleText = "Admin Management";
            } else if (filterRole == UserRole.COACH) {
                titleText = "Coach Management";
            }
        }
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(UIThemeUtil.HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Top panel - Search
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(UIThemeUtil.LABEL_FONT);
        topPanel.add(searchLabel);
        
        searchField = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        searchField.setFont(UIThemeUtil.INPUT_FONT);
        searchField.setBackground(new Color(70, 70, 70));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchField.setOpaque(false);
        searchField.addActionListener(e -> searchUsers());
        topPanel.add(searchField);
        
        JButton searchButton = UIThemeUtil.createStyledButton("SEARCH",
            new Color(70, 130, 180), new Color(100, 150, 200));
        searchButton.addActionListener(e -> searchUsers());
        topPanel.add(searchButton);
        
        headerPanel.add(topPanel, BorderLayout.EAST);
        glassPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel - Table
        String[] columns = {"ID", "Username", "Full Name", "Email", "Mobile",
                           "Role", "Branch", "Active", "Last Login"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };
        
        usersTable = new JTable(tableModel);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersTable.setRowHeight(30);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setBackground(new Color(40, 40, 40));
        usersTable.setForeground(Color.WHITE);
        usersTable.setGridColor(new Color(60, 60, 60));
        usersTable.getTableHeader().setBackground(new Color(30, 30, 30));
        usersTable.getTableHeader().setForeground(Color.WHITE);
        usersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Hide ID column (internal use only)
        usersTable.getColumnModel().getColumn(0).setMinWidth(0);
        usersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        usersTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        glassPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        addButton = UIThemeUtil.createStyledButton("ADD USER",
            new Color(50, 150, 50), new Color(70, 170, 70));
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.addActionListener(e -> addUser());
        buttonPanel.add(addButton);
        
        editButton = UIThemeUtil.createStyledButton("EDIT",
            new Color(70, 130, 180), new Color(100, 150, 200));
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editUser());
        buttonPanel.add(editButton);
        
        deleteButton = UIThemeUtil.createStyledButton("DELETE",
            new Color(150, 50, 50), new Color(170, 70, 70));
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteUser());
        buttonPanel.add(deleteButton);
        
        resetPasswordButton = UIThemeUtil.createStyledButton("RESET PASSWORD",
            new Color(180, 120, 50), new Color(200, 140, 70));
        resetPasswordButton.setPreferredSize(new Dimension(180, 45));
        resetPasswordButton.addActionListener(e -> resetPassword());
        buttonPanel.add(resetPasswordButton);
        
        refreshButton = UIThemeUtil.createStyledButton("REFRESH",
            new Color(100, 100, 100), new Color(120, 120, 120));
        refreshButton.setPreferredSize(new Dimension(120, 45));
        refreshButton.addActionListener(e -> loadUsers());
        buttonPanel.add(refreshButton);
        
        glassPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(glassPanel, BorderLayout.CENTER);
    }
    
    private void loadUsers() {
        tableModel.setRowCount(0); // Clear table
        
        try {
            List<User> users;
            
            if (filterRole != null) {
                users = userService.getUsersByRole(filterRole);
            } else {
                // Load all non-owner users
                List<User> admins = userService.getUsersByRole(UserRole.ADMIN);
                List<User> coaches = userService.getUsersByRole(UserRole.COACH);
                users = admins;
                users.addAll(coaches);
            }
            
            for (User user : users) {
                // Get branch name if branch ID exists
                String branchName = "N/A";
                if (user.getBranchId() != null) {
                    try {
                        app.model.Branch branch = new app.service.BranchService().getBranchById(user.getBranchId());
                        branchName = branch != null ? branch.getBranchName() : "Branch #" + user.getBranchId();
                    } catch (Exception ex) {
                        branchName = "Branch #" + user.getBranchId();
                    }
                }
                
                tableModel.addRow(new Object[]{
                    user.getId(),
                    user.getUsername(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getEmail(),
                    user.getMobile(),
                    user.getRole(),
                    branchName,
                    user.isActive() ? "Active" : "Inactive",
                    user.getLastLogin() != null ? user.getLastLogin() : "Never"
                });
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void searchUsers() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }
        
        tableModel.setRowCount(0);
        
        try {
            List<User> users;
            
            if (filterRole != null) {
                users = userService.getUsersByRole(filterRole);
            } else {
                List<User> admins = userService.getUsersByRole(UserRole.ADMIN);
                List<User> coaches = userService.getUsersByRole(UserRole.COACH);
                users = admins;
                users.addAll(coaches);
            }
            
            for (User user : users) {
                String searchText = (user.getUsername() + " " + user.getFirstName() + " " +
                                   user.getLastName() + " " + user.getEmail()).toLowerCase();
                
                if (searchText.contains(keyword)) {
                    tableModel.addRow(new Object[]{
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getEmail(),
                        user.getMobile(),
                        user.getRole(),
                        user.getBranchId() != null ? user.getBranchId() : "N/A",
                        user.isActive() ? "Active" : "Inactive",
                        user.getLastLogin() != null ? user.getLastLogin() : "Never"
                    });
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching users: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addUser() {
        Window window = SwingUtilities.getWindowAncestor(this);
        ViewEditUserDialog dialog = new ViewEditUserDialog(window, filterRole);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadUsers(); // Refresh table
        }
    }
    
    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            User user = userService.findById(userId).orElse(null);
            
            if (user == null) {
                JOptionPane.showMessageDialog(this, "User not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ViewEditUserDialog dialog = new ViewEditUserDialog(SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            
            if (dialog.isSaved()) {
                loadUsers(); // Refresh table
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error editing user: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            String ipAddress = "127.0.0.1"; // TODO: Get real IP
            
            boolean success = userService.deleteUser(userId, ipAddress);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "User deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void resetPassword() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to reset password.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        String newPassword = JOptionPane.showInputDialog(this,
            "Enter new password for user: " + username,
            "Reset Password",
            JOptionPane.PLAIN_MESSAGE);
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return;
        }
        
        try {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            String ipAddress = "127.0.0.1"; // TODO: Get real IP
            
            // TODO: Implement password reset in AuthenticationService
            JOptionPane.showMessageDialog(this,
                "Password reset functionality coming soon!",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error resetting password: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Main method for testing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageUsersView view = new ManageUsersView();
            view.setVisible(true);
        });
    }
}
