package app.views.shared;

import app.model.Member;
import app.model.User;
import app.service.MemberService;
import app.service.UserService;
import app.util.SessionManager;
import app.util.UIThemeUtil;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Manage Members View - Main interface for member CRUD operations.
 * 
 * Features:
 * - JTable with member list
 * - Search functionality
 * - Add/Edit/Delete operations
 * - Branch filtering (for admins)
 * - Status filtering (active/expired/expiring)
 * - Coach assignment view
 * - Glass-morphism design
 * 
 * @author Gym Management System
 * @version 2.0
 */
public class ManageMembersView extends JPanel {
    
    private final MemberService memberService;
    private final UserService userService;
    private final SessionManager sessionManager;
    private final int branchId; // For branch-restricted access
    
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    /**
     * Constructor for branch-specific view (Admin/Coach).
     */
    public ManageMembersView(int branchId) {
        this.memberService = new MemberService();
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance();
        this.branchId = branchId;
        
        initComponents();
        loadMembers();
    }
    
    /**
     * Constructor for all-branch view (Owner).
     */
    public ManageMembersView() {
        this(-1); // -1 means all branches
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
        
        // Header with title and search
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Member Management");
        titleLabel.setFont(UIThemeUtil.HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Top panel - Search and filters
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
        searchField.addActionListener(e -> searchMembers());
        topPanel.add(searchField);
        
        JButton searchButton = UIThemeUtil.createStyledButton("SEARCH", 
            new Color(70, 130, 180), new Color(100, 150, 200));
        searchButton.addActionListener(e -> searchMembers());
        topPanel.add(searchButton);
        
        // Status filter
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(UIThemeUtil.LABEL_FONT);
        topPanel.add(statusLabel);
        
        statusFilter = new JComboBox<>(new String[]{"All", "Active", "Expired", "Expiring Soon"});
        styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> loadMembers());
        topPanel.add(statusFilter);
        
        headerPanel.add(topPanel, BorderLayout.EAST);
        glassPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel - Table
        String[] columns = {"ID", "Random ID", "Full Name", "Mobile", "Email", 
                           "Gender", "Payment", "Period", "Start Date", "End Date", 
                           "Coach", "Status", "Branch ID"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };
        
        membersTable = new JTable(tableModel);
        membersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        membersTable.setRowHeight(30);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setBackground(new Color(40, 40, 40));
        membersTable.setForeground(Color.WHITE);
        membersTable.setGridColor(new Color(60, 60, 60));
        membersTable.getTableHeader().setBackground(new Color(30, 30, 30));
        membersTable.getTableHeader().setForeground(Color.WHITE);
        membersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Hide ID column (internal use only)
        membersTable.getColumnModel().getColumn(0).setMinWidth(0);
        membersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        glassPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        addButton = UIThemeUtil.createStyledButton("ADD MEMBER", 
            new Color(50, 150, 50), new Color(70, 170, 70));
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.addActionListener(e -> addMember());
        buttonPanel.add(addButton);
        
        editButton = UIThemeUtil.createStyledButton("EDIT", 
            new Color(70, 130, 180), new Color(100, 150, 200));
        editButton.setPreferredSize(new Dimension(120, 45));
        editButton.addActionListener(e -> editMember());
        buttonPanel.add(editButton);
        
        deleteButton = UIThemeUtil.createStyledButton("DELETE", 
            new Color(150, 50, 50), new Color(170, 70, 70));
        deleteButton.setPreferredSize(new Dimension(120, 45));
        deleteButton.addActionListener(e -> deleteMember());
        buttonPanel.add(deleteButton);
        
        refreshButton = UIThemeUtil.createStyledButton("REFRESH", 
            new Color(100, 100, 100), new Color(120, 120, 120));
        refreshButton.setPreferredSize(new Dimension(120, 45));
        refreshButton.addActionListener(e -> loadMembers());
        buttonPanel.add(refreshButton);
        
        glassPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Check permissions
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.isCoach()) {
            // Coaches can only view, not add/edit/delete
            addButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        
        add(glassPanel, BorderLayout.CENTER);
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(50, 50, 50));
        combo.setForeground(Color.WHITE);
    }
    
    private void loadMembers() {
        tableModel.setRowCount(0); // Clear table
        
        try {
            List<Member> members;
            User currentUser = sessionManager.getCurrentUser();
            
            if (currentUser == null) {
                // If no session, try to load all members anyway (development mode)
                System.out.println("⚠️ Warning: No session found, loading all members");
                members = memberService.getAllMembers();
            } else {
                // Determine which members to load based on role and branch
                if (currentUser.isCoach()) {
                    members = memberService.getMembersByCoach(currentUser.getId());
                } else if (currentUser.isOwner()) {
                    // Owner - show all members
                    members = memberService.getAllMembers();
                } else if (branchId > 0) {
                    members = memberService.getMembersByBranch(branchId);
                } else {
                    // Admin with no specific branch - show all
                    members = memberService.getAllMembers();
                }
            }
            
            // Apply status filter
            String selectedStatus = (String) statusFilter.getSelectedItem();
            
            for (Member member : members) {
                // Filter by status
                if (!selectedStatus.equals("All")) {
                    if (selectedStatus.equals("Active") && !member.isActive()) continue;
                    if (selectedStatus.equals("Expired") && member.isActive()) continue;
                    // TODO: Add "Expiring Soon" logic using DateUtil
                }
                
                // Get coach name
                String coachName = "";
                if (member.getAssignedCoach() != null) {
                    try {
                        User coach = userService.findById(member.getAssignedCoach()).orElse(null);
                        if (coach != null) {
                            coachName = coach.getFirstName() + " " + coach.getLastName();
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading coach: " + e.getMessage());
                    }
                }
                
                tableModel.addRow(new Object[]{
                    member.getId(),
                    member.getRandomId(),
                    member.getFirstName() + " " + member.getLastName(),
                    member.getMobile(),
                    member.getEmail() != null ? member.getEmail() : "",
                    member.getGender(),
                    member.getPayment(),
                    member.getPeriod(),
                    member.getStartDate(),
                    member.getEndDate(),
                    coachName,
                    member.isActive() ? "Active" : "Inactive",
                    member.getAssignedBranch()
                });
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void searchMembers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadMembers();
            return;
        }
        
        tableModel.setRowCount(0);
        
        try {
            Integer searchBranchId = branchId > 0 ? branchId : null;
            List<Member> members = memberService.searchMembers(keyword, searchBranchId);
            
            for (Member member : members) {
                // Get coach name
                String coachName = "";
                if (member.getAssignedCoach() != null) {
                    User coach = userService.findById(member.getAssignedCoach()).orElse(null);
                    if (coach != null) {
                        coachName = coach.getFirstName() + " " + coach.getLastName();
                    }
                }
                
                tableModel.addRow(new Object[]{
                    member.getId(),
                    member.getRandomId(),
                    member.getFirstName() + " " + member.getLastName(),
                    member.getMobile(),
                    member.getEmail() != null ? member.getEmail() : "",
                    member.getGender(),
                    member.getPayment(),
                    member.getPeriod(),
                    member.getStartDate(),
                    member.getEndDate(),
                    coachName,
                    member.isActive() ? "Active" : "Inactive",
                    member.getAssignedBranch()
                });
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching members: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addMember() {
        int targetBranchId = branchId > 0 ? branchId : 1; // Default to branch 1 if owner
        
        // Get parent window - if null, use fallback
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow == null) {
            // Fallback: find the frame from the root pane
            parentWindow = (Window) SwingUtilities.getRoot(this);
        }
        
        ViewEditMemberDialog dialog = new ViewEditMemberDialog(parentWindow, targetBranchId);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadMembers(); // Refresh table
        }
    }
    
    private void editMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int memberId = (int) tableModel.getValueAt(selectedRow, 0);
            Member member = memberService.findById(memberId).orElse(null);
            
            if (member == null) {
                JOptionPane.showMessageDialog(this, "Member not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get parent window - if null, use JOptionPane to find frame
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow == null) {
                // Fallback: find the frame from the root pane
                parentWindow = (Window) SwingUtilities.getRoot(this);
            }
            
            ViewEditMemberDialog dialog = new ViewEditMemberDialog(parentWindow, member, member.getAssignedBranch());
            dialog.setVisible(true);
            
            if (dialog.isSaved()) {
                loadMembers(); // Refresh table
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error editing member: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this member?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            int memberId = (int) tableModel.getValueAt(selectedRow, 0);
            String ipAddress = "127.0.0.1"; // TODO: Get real IP
            
            boolean success = memberService.deleteMember(memberId, ipAddress);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Member deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMembers(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete member.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting member: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Main method for testing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageMembersView view = new ManageMembersView(1); // Test with branch 1
            view.setVisible(true);
        });
    }
}
