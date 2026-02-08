package app.views.admin;

import app.dao.BranchDAO;
import app.dao.UserDAO;
import app.model.Branch;
import app.model.User;
import app.model.UserRole;
import app.views.shared.ViewEditUserDialog;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * ManageCoachesView - Branch-restricted coach management for Admins
 * Shows only coaches from the admin's branch
 */
public class ManageCoachesView extends JPanel {
    
    private final Integer branchId;
    private final UserDAO userDAO;
    private final BranchDAO branchDAO;
    
    private JTable coachTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public ManageCoachesView(Integer branchId) {
        this.branchId = branchId;
        this.userDAO = new UserDAO();
        this.branchDAO = new BranchDAO();
        
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        initComponents();
        loadCoaches();
    }
    
    private void initComponents() {
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Main Content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setOpaque(false);
        
        // Action Panel
        JPanel actionPanel = createActionPanel();
        contentPanel.add(actionPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Manage Coaches - " + getBranchName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(220, 220, 220));
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        
        // Left side - Add button
        JButton addBtn = createGlassButton("Add New Coach");
        addBtn.addActionListener(e -> addCoach());
        panel.add(addBtn, BorderLayout.WEST);
        
        // Right side - Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchLabel.setForeground(new Color(180, 180, 180));
        searchPanel.add(searchLabel);
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 40));
        styleSearchField(searchField);
        searchField.addActionListener(e -> searchCoaches());
        searchPanel.add(searchField);
        
        JButton searchBtn = createGlassButton("Search");
        searchBtn.addActionListener(e -> searchCoaches());
        searchPanel.add(searchBtn);
        
        JButton refreshBtn = createGlassButton("Refresh");
        refreshBtn.addActionListener(e -> loadCoaches());
        searchPanel.add(refreshBtn);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        // Container with blur background
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gray blur background
                g2.setColor(new Color(50, 50, 50, 160));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Simple gray border
                g2.setColor(new Color(100, 100, 100, 150));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Create table
        String[] columns = {"ID", "First Name", "Last Name", "Username", "Email", "Mobile", "Branch", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only Actions column
            }
        };
        
        coachTable = new JTable(tableModel);
        styleTable(coachTable);
        
        // Column widths
        coachTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        coachTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        coachTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        coachTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        coachTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        coachTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        coachTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        coachTable.getColumnModel().getColumn(7).setPreferredWidth(180);
        
        // Actions column with buttons
        coachTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        coachTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(coachTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        container.add(panel, BorderLayout.CENTER);
        
        return container;
    }
    
    private void loadCoaches() {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                
                List<User> coaches = userDAO.findByRoleAndBranch(UserRole.COACH, branchId);
                
                for (User coach : coaches) {
                    String branchName = getBranchName();
                    
                    tableModel.addRow(new Object[]{
                        coach.getId(),
                        coach.getFirstName(),
                        coach.getLastName(),
                        coach.getUsername(),
                        coach.getEmail(),
                        coach.getMobile() != null ? coach.getMobile() : "N/A",
                        branchName,
                        "actions" // Placeholder for button renderer
                    });
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading coaches: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    private void searchCoaches() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadCoaches();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            
            List<User> coaches = userDAO.findByRoleAndBranch(UserRole.COACH, branchId);
            
            for (User coach : coaches) {
                boolean matches = coach.getFirstName().toLowerCase().contains(searchText)
                    || coach.getLastName().toLowerCase().contains(searchText)
                    || coach.getUsername().toLowerCase().contains(searchText)
                    || coach.getEmail().toLowerCase().contains(searchText);
                
                if (matches) {
                    String branchName = getBranchName();
                    
                    tableModel.addRow(new Object[]{
                        coach.getId(),
                        coach.getFirstName(),
                        coach.getLastName(),
                        coach.getUsername(),
                        coach.getEmail(),
                        coach.getMobile() != null ? coach.getMobile() : "N/A",
                        branchName,
                        "actions"
                    });
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching coaches: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addCoach() {
        SwingUtilities.invokeLater(() -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            ViewEditUserDialog dialog = new ViewEditUserDialog(
                parentWindow,
                UserRole.COACH
            );
            
            dialog.setVisible(true);
            
            // Refresh after dialog closes
            SwingUtilities.invokeLater(() -> {
                this.revalidate();
                this.repaint();
                loadCoaches();
            });
        });
    }
    
    private void editCoach(int coachId) {
        SwingUtilities.invokeLater(() -> {
            try {
                java.util.Optional<User> userOpt = userDAO.findById(coachId);
                
                if (userOpt.isPresent()) {
                    User coach = userOpt.get();
                    
                    // Verify coach belongs to this branch
                    if (!branchId.equals(coach.getBranchId())) {
                        JOptionPane.showMessageDialog(this,
                            "You can only edit coaches from your branch.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    Window parentWindow = SwingUtilities.getWindowAncestor(this);
                    ViewEditUserDialog dialog = new ViewEditUserDialog(
                        parentWindow,
                        coach,
                        UserRole.COACH
                    );
                    
                    dialog.setVisible(true);
                    
                    // Refresh after dialog closes
                    SwingUtilities.invokeLater(() -> {
                        this.revalidate();
                        this.repaint();
                        loadCoaches();
                    });
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading coach: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void deleteCoach(int coachId) {
        try {
            java.util.Optional<User> userOpt = userDAO.findById(coachId);
            
            if (userOpt.isPresent()) {
                User coach = userOpt.get();
                
                // Verify coach belongs to this branch
                if (!branchId.equals(coach.getBranchId())) {
                    JOptionPane.showMessageDialog(this,
                        "You can only delete coaches from your branch.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete coach: " + coach.getFirstName() + " " + coach.getLastName() + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = userDAO.delete(coachId);
                    
                    if (deleted) {
                        JOptionPane.showMessageDialog(this,
                            "Coach deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadCoaches();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete coach.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting coach: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getBranchName() {
        if (branchId == null) {
            return "Not Assigned";
        }
        
        try {
            return branchDAO.findById(branchId)
                .map(Branch::getName)
                .orElse("Unknown Branch");
        } catch (Exception e) {
            return "Branch #" + branchId;
        }
    }
    
    // Styling Methods
    
    private JButton createGlassButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(80, 80, 80, 180));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(100, 100, 100, 160));
                } else {
                    g2.setColor(new Color(70, 70, 70, 140));
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.setColor(new Color(150, 150, 150, 180));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(220, 220, 220));
        button.setPreferredSize(new Dimension(140, 42));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void styleSearchField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(45, 45, 50));
        field.setForeground(new Color(220, 220, 220));
        field.setCaretColor(new Color(200, 200, 200));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 80), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        field.setOpaque(true);
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setBackground(new Color(60, 60, 60, 140));
        table.setForeground(new Color(220, 220, 220));
        table.setGridColor(new Color(80, 80, 80, 100));
        table.setSelectionBackground(new Color(100, 100, 100, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setOpaque(false);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(50, 50, 50));
        header.setForeground(new Color(200, 200, 200));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setOpaque(true);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(new Color(60, 60, 60, 140));
        centerRenderer.setForeground(new Color(220, 220, 220));
        centerRenderer.setOpaque(false);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    }
    
    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(new Color(35, 35, 42));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 88), 2, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(new Color(35, 35, 42));
    }
    
    // Button Renderer and Editor for Actions Column
    
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;
        
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            setOpaque(false);
            
            editButton = createSmallButton("Edit");
            deleteButton = createSmallButton("Delete");
            deleteButton.setForeground(Color.WHITE);
            
            add(editButton);
            add(deleteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            return this;
        }
        
        private JButton createSmallButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(70, 28));
            btn.setFocusPainted(false);
            btn.setBorderPainted(true);
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            
            if (text.equals("Edit")) {
                btn.setBackground(new Color(70, 70, 80));
                btn.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 100)));
            } else {
                btn.setBackground(new Color(180, 60, 60));
                btn.setBorder(BorderFactory.createLineBorder(new Color(160, 50, 50)));
            }
            
            return btn;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editButton;
        private JButton deleteButton;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            panel.setOpaque(false);
            
            editButton = createSmallButton("Edit");
            editButton.addActionListener(e -> {
                fireEditingStopped();
                int coachId = (Integer) tableModel.getValueAt(currentRow, 0);
                editCoach(coachId);
            });
            
            deleteButton = createSmallButton("Delete");
            deleteButton.setForeground(Color.WHITE);
            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                int coachId = (Integer) tableModel.getValueAt(currentRow, 0);
                deleteCoach(coachId);
            });
            
            panel.add(editButton);
            panel.add(deleteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                    boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
        
        private JButton createSmallButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(70, 28));
            btn.setFocusPainted(false);
            btn.setBorderPainted(true);
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            
            if (text.equals("Edit")) {
                btn.setBackground(new Color(70, 70, 80));
                btn.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 100)));
            } else {
                btn.setBackground(new Color(180, 60, 60));
                btn.setBorder(BorderFactory.createLineBorder(new Color(160, 50, 50)));
            }
            
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }
    }
}
