package app.views.shared;

import app.model.Branch;
import app.service.BranchService;
import app.service.MemberService;
import app.service.UserService;
import app.util.SessionManager;
import app.util.UIThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Branch Management View - Owner only
 * Displays all branches with member/coach counts
 * Supports Add/Edit/Delete operations
 */
public class ManageBranchesView extends JPanel {
    
    private final BranchService branchService;
    private final MemberService memberService;
    private final UserService userService;
    private final SessionManager sessionManager;
    
    private JTable branchTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, closeButton;
    
    public ManageBranchesView() {
        this.branchService = new BranchService();
        this.memberService = new MemberService();
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance();
        
        initComponents();
        loadBranches();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create glass panel wrapper with background
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Blur gray glass effect
                g2d.setColor(new Color(50, 50, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                // Subtle border
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.dispose();
            }
        };
        glassPanel.setLayout(new BorderLayout(15, 15));
        glassPanel.setOpaque(false);
        glassPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        glassPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        JPanel tablePanel = createTablePanel();
        glassPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        glassPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(glassPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Branch Management");
        titleLabel.setFont(UIThemeUtil.HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(UIThemeUtil.LABEL_FONT);
        searchPanel.add(searchLabel);
        
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
        searchField.addActionListener(e -> searchBranches());
        searchPanel.add(searchField);
        
        JButton searchButton = UIThemeUtil.createStyledButton("SEARCH", UIThemeUtil.OWNER_ACCENT, UIThemeUtil.OWNER_ACCENT.darker());
        searchButton.addActionListener(e -> searchBranches());
        searchPanel.add(searchButton);
        
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        
        // Table columns: ID (hidden), Branch Name, Address, Phone, Active, Member Count, Coach Count
        String[] columns = {"ID", "Branch Name", "Address", "Phone", "Active", "Members", "Coaches"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        branchTable = new JTable(tableModel);
        branchTable.setFont(UIThemeUtil.INPUT_FONT);
        branchTable.setRowHeight(35);
        branchTable.setBackground(new Color(40, 40, 40));
        branchTable.setForeground(Color.WHITE);
        branchTable.setGridColor(new Color(60, 60, 60));
        branchTable.getTableHeader().setFont(UIThemeUtil.LABEL_FONT);
        branchTable.getTableHeader().setBackground(new Color(30, 30, 30));
        branchTable.getTableHeader().setForeground(Color.WHITE);
        branchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Hide ID column
        branchTable.getColumnModel().getColumn(0).setMinWidth(0);
        branchTable.getColumnModel().getColumn(0).setMaxWidth(0);
        branchTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        branchTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Branch Name
        branchTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Address
        branchTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Phone
        branchTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Active
        branchTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Members
        branchTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Coaches
        
        JScrollPane scrollPane = new JScrollPane(branchTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        addButton = UIThemeUtil.createStyledButton("ADD BRANCH", UIThemeUtil.SUCCESS_COLOR, UIThemeUtil.SUCCESS_COLOR.darker());
        addButton.addActionListener(e -> addBranch());
        buttonPanel.add(addButton);
        
        editButton = UIThemeUtil.createStyledButton("EDIT", UIThemeUtil.OWNER_ACCENT, UIThemeUtil.OWNER_ACCENT.darker());
        editButton.addActionListener(e -> editBranch());
        buttonPanel.add(editButton);
        
        deleteButton = UIThemeUtil.createStyledButton("DELETE", UIThemeUtil.ERROR_COLOR, UIThemeUtil.ERROR_COLOR.darker());
        deleteButton.addActionListener(e -> deleteBranch());
        buttonPanel.add(deleteButton);
        
        refreshButton = UIThemeUtil.createStyledButton("REFRESH", UIThemeUtil.OWNER_ACCENT, UIThemeUtil.OWNER_ACCENT.darker());
        refreshButton.addActionListener(e -> loadBranches());
        buttonPanel.add(refreshButton);
        
        // Remove close button since this is embedded in dashboard now
        
        return buttonPanel;
    }
    
    private void loadBranches() {
        try {
            tableModel.setRowCount(0);
            List<Branch> branches = branchService.getAllBranches();
            
            for (Branch branch : branches) {
                // Get member and coach counts
                int memberCount = memberService.getMembersByBranch(branch.getBranchId()).size();
                int coachCount = userService.getUsersByBranchAndRole(branch.getBranchId(), app.model.UserRole.COACH).size();
                
                Object[] row = {
                    branch.getBranchId(),
                    branch.getBranchName(),
                    branch.getAddress(),
                    branch.getPhone(),
                    branch.isActive() ? "Yes" : "No",
                    memberCount,
                    coachCount
                };
                tableModel.addRow(row);
            }
            
            branchTable.revalidate();
            branchTable.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading branches: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void searchBranches() {
        String keyword = searchField.getText().trim().toLowerCase();
        
        if (keyword.isEmpty()) {
            loadBranches();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            List<Branch> branches = branchService.getAllBranches();
            
            for (Branch branch : branches) {
                boolean matches = branch.getBranchName().toLowerCase().contains(keyword) ||
                                  branch.getAddress().toLowerCase().contains(keyword) ||
                                  branch.getPhone().toLowerCase().contains(keyword);
                
                if (matches) {
                    int memberCount = memberService.getMembersByBranch(branch.getBranchId()).size();
                    int coachCount = userService.getUsersByBranchAndRole(branch.getBranchId(), app.model.UserRole.COACH).size();
                    
                    Object[] row = {
                        branch.getBranchId(),
                        branch.getBranchName(),
                        branch.getAddress(),
                        branch.getPhone(),
                        branch.isActive() ? "Yes" : "No",
                        memberCount,
                        coachCount
                    };
                    tableModel.addRow(row);
                }
            }
            
            branchTable.revalidate();
            branchTable.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching branches: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addBranch() {
        Window window = SwingUtilities.getWindowAncestor(this);
        ViewEditBranchDialog dialog = new ViewEditBranchDialog(window, null);
        dialog.setVisible(true);
        loadBranches(); // Refresh after dialog closes
    }
    
    private void editBranch() {
        int selectedRow = branchTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a branch to edit",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int branchId = (int) tableModel.getValueAt(selectedRow, 0);
            Branch branch = branchService.getBranchById(branchId);
            
            if (branch != null) {
                Window window = SwingUtilities.getWindowAncestor(this);
                ViewEditBranchDialog dialog = new ViewEditBranchDialog(window, branch);
                dialog.setVisible(true);
                loadBranches(); // Refresh after dialog closes
            } else {
                JOptionPane.showMessageDialog(this,
                    "Branch not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error editing branch: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteBranch() {
        int selectedRow = branchTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a branch to delete",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int branchId = (int) tableModel.getValueAt(selectedRow, 0);
            String branchName = (String) tableModel.getValueAt(selectedRow, 1);
            int memberCount = (int) tableModel.getValueAt(selectedRow, 5);
            int coachCount = (int) tableModel.getValueAt(selectedRow, 6);
            
            // Check if branch has members or coaches
            if (memberCount > 0 || coachCount > 0) {
                JOptionPane.showMessageDialog(this,
                    "Cannot delete branch with assigned members or coaches.\n" +
                    "Members: " + memberCount + ", Coaches: " + coachCount + "\n" +
                    "Please reassign or deactivate them first.",
                    "Cannot Delete",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete branch '" + branchName + "'?\n" +
                "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                branchService.deleteBranch(branchId, "127.0.0.1");
                JOptionPane.showMessageDialog(this,
                    "Branch deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadBranches();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting branch: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
