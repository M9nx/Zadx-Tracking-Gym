package app.views;

import app.dao.AuditLogDAO;
import app.dao.UserDAO;
import app.model.AuditLog;
import app.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AuditLogsView - Displays system audit logs with modern UI
 * Matches the styling of SystemSettingsView and ManageUsersView
 */
public class AuditLogsView extends JPanel {
    
    private final AuditLogDAO auditLogDAO;
    private final UserDAO userDAO;
    
    private JTable auditTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> actionFilterCombo;
    private JTextField searchField;
    private JSpinner limitSpinner;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final String[] ACTION_FILTERS = {
        "All Actions",
        "LOGIN_SUCCESS",
        "LOGIN_FAILED",
        "LOGOUT",
        "PASSWORD_RESET",
        "PASSWORD_CHANGE",
        "USER_CREATE",
        "USER_UPDATE",
        "USER_DELETE",
        "MEMBER_CREATE",
        "MEMBER_UPDATE",
        "MEMBER_DELETE"
    };
    
    public AuditLogsView() {
        this.auditLogDAO = new AuditLogDAO();
        this.userDAO = new UserDAO();
        
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(23, 23, 28));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        initComponents();
        loadAuditLogs();
    }
    
    private void initComponents() {
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Main Content Panel with filters and table
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        
        // Filter Panel
        JPanel filterPanel = createFilterPanel();
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Audit Logs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(230, 230, 240));
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Refresh button
        JButton refreshBtn = createGlassButton("Refresh");
        refreshBtn.addActionListener(e -> loadAuditLogs());
        panel.add(refreshBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Left side - filters
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftFilters.setOpaque(false);
        
        // Action filter
        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        actionLabel.setForeground(new Color(220, 220, 230));
        leftFilters.add(actionLabel);
        
        actionFilterCombo = new JComboBox<>(ACTION_FILTERS);
        actionFilterCombo.setPreferredSize(new Dimension(200, 40));
        styleComboBox(actionFilterCombo);
        actionFilterCombo.addActionListener(e -> loadAuditLogs());
        leftFilters.add(actionFilterCombo);
        
        // Limit spinner
        JLabel limitLabel = new JLabel("Show:");
        limitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        limitLabel.setForeground(new Color(220, 220, 230));
        leftFilters.add(limitLabel);
        
        limitSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        limitSpinner.setPreferredSize(new Dimension(100, 40));
        styleSpinner(limitSpinner);
        limitSpinner.addChangeListener(e -> loadAuditLogs());
        leftFilters.add(limitSpinner);
        
        panel.add(leftFilters, BorderLayout.WEST);
        
        // Right side - search
        JPanel rightFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightFilters.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchLabel.setForeground(new Color(220, 220, 230));
        rightFilters.add(searchLabel);
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 40));
        styleSearchField(searchField);
        searchField.addActionListener(e -> filterTable());
        rightFilters.add(searchField);
        
        JButton searchBtn = createGlassButton("Search");
        searchBtn.addActionListener(e -> filterTable());
        rightFilters.add(searchBtn);
        
        panel.add(rightFilters, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Create table model
        String[] columns = {"ID", "Timestamp", "User", "Action", "Target", "Details", "IP Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        auditTable = new JTable(tableModel);
        styleTable(auditTable);
        
        // Column widths
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(250);
        auditTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(auditTable);
        styleScrollPane(scrollPane);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel statusLabel = new JLabel("Total logs: 0");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(200, 200, 210));
        statusLabel.setName("statusLabel");
        panel.add(statusLabel);
        
        return panel;
    }
    
    private void loadAuditLogs() {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                
                String selectedAction = (String) actionFilterCombo.getSelectedItem();
                int limit = (Integer) limitSpinner.getValue();
                
                List<AuditLog> logs;
                
                if ("All Actions".equals(selectedAction)) {
                    logs = auditLogDAO.findRecent(limit);
                } else {
                    logs = auditLogDAO.findByAction(selectedAction, limit);
                }
                
                for (AuditLog log : logs) {
                    String username = getUsernameById(log.getUserId());
                    String target = formatTarget(log.getTargetType(), log.getTargetId());
                    String timestamp = log.getTimestamp() != null ? 
                        log.getTimestamp().format(DATE_FORMATTER) : "N/A";
                    
                    tableModel.addRow(new Object[]{
                        log.getAuditId(),
                        timestamp,
                        username,
                        log.getAction(),
                        target,
                        log.getDetails(),
                        log.getIpAddress() != null ? log.getIpAddress() : "N/A"
                    });
                }
                
                updateStatusLabel(logs.size());
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading audit logs: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    private void filterTable() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadAuditLogs();
            return;
        }
        
        // Simple client-side filtering
        DefaultTableModel model = (DefaultTableModel) auditTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            boolean found = false;
            for (int j = 0; j < model.getColumnCount(); j++) {
                Object value = model.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(searchText)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                model.removeRow(i);
            }
        }
        
        updateStatusLabel(model.getRowCount());
    }
    
    private String getUsernameById(Integer userId) {
        if (userId == null) {
            return "SYSTEM";
        }
        
        try {
            return userDAO.findById(userId)
                .map(User::getUsername)
                .orElse("Unknown");
        } catch (Exception e) {
            return "User#" + userId;
        }
    }
    
    private String formatTarget(String targetType, Integer targetId) {
        if (targetType == null || targetId == null) {
            return "N/A";
        }
        return targetType + " #" + targetId;
    }
    
    private void updateStatusLabel(int count) {
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                findAndUpdateStatusLabel((JPanel) comp, count);
            }
        }
    }
    
    private void findAndUpdateStatusLabel(JPanel panel, int count) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel && "statusLabel".equals(comp.getName())) {
                ((JLabel) comp).setText("Total logs: " + count);
                return;
            } else if (comp instanceof JPanel) {
                findAndUpdateStatusLabel((JPanel) comp, count);
            }
        }
    }
    
    // Styling methods
    
    private JButton createGlassButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(139, 92, 246, 200));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(139, 92, 246, 180));
                } else {
                    g2.setColor(new Color(139, 92, 246, 150));
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(65, 65, 72));
        combo.setForeground(new Color(240, 240, 250));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 110), 2, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        combo.setFocusable(true);
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBackground(new Color(65, 65, 72));
            textField.setForeground(new Color(240, 240, 250));
            textField.setCaretColor(new Color(139, 92, 246));
            textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
        spinner.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 110), 2, true));
    }
    
    private void styleSearchField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(65, 65, 72));
        field.setForeground(new Color(240, 240, 250));
        field.setCaretColor(new Color(139, 92, 246));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 110), 2, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setBackground(new Color(35, 35, 42));
        table.setForeground(new Color(240, 240, 250));
        table.setGridColor(new Color(60, 60, 68));
        table.setSelectionBackground(new Color(139, 92, 246, 100));
        table.setSelectionForeground(new Color(255, 255, 255));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(45, 45, 52));
        header.setForeground(new Color(220, 220, 230));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(139, 92, 246)));
        
        // Center align renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(new Color(35, 35, 42));
        centerRenderer.setForeground(new Color(240, 240, 250));
        
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    }
    
    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(new Color(35, 35, 42));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 88), 2, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(new Color(35, 35, 42));
        
        // Style scrollbars
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setBackground(new Color(35, 35, 42));
        vertical.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 110);
                this.trackColor = new Color(35, 35, 42);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        
        JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
        horizontal.setBackground(new Color(35, 35, 42));
        horizontal.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 110);
                this.trackColor = new Color(35, 35, 42);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
    }
}
