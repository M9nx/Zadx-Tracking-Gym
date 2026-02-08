package app.views.coach;

import Project.ConnectionProvider;
import app.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Coach Members View - Shows members assigned to the coach
 * Read-only view for coaches to see their assigned members
 */
public class CoachMembersView extends JPanel {
    
    private final User currentCoach;
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel totalLabel;
    private JLabel activeLabel;
    
    public CoachMembersView(User coach) {
        this.currentCoach = coach;
        
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadMembers();
        updateStatistics();
    }
    
    private void initComponents() {
        // Main panel with blur background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(40, 40, 40, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Statistics Cards
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Assigned Members");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(220, 220, 220));
        
        JLabel subtitleLabel = new JLabel("View members assigned to you");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout(15, 15));
        statsPanel.setOpaque(false);
        
        // Statistics cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 100));
        
        totalLabel = new JLabel("0");
        activeLabel = new JLabel("0");
        
        cardsPanel.add(createStatCard("Total Members", totalLabel, new Color(150, 150, 150)));
        cardsPanel.add(createStatCard("Active Members", activeLabel, new Color(46, 204, 113)));
        
        statsPanel.add(cardsPanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        statsPanel.add(tablePanel, BorderLayout.CENTER);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(60, 60, 60, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(new Color(180, 180, 180));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(50, 50, 50, 160));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header with search
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        
        JLabel label = new JLabel("Members List");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(200, 200, 200));
        headerPanel.add(label, BorderLayout.WEST);
        
        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(new Color(45, 45, 50));
        searchField.setForeground(new Color(220, 220, 220));
        searchField.setCaretColor(new Color(200, 200, 200));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 80), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadMembers();
            }
        });
        
        JButton refreshBtn = createGlassButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadMembers();
            updateStatistics();
        });
        
        searchPanel.add(new JLabel("🔍"));
        searchPanel.add(searchField);
        searchPanel.add(refreshBtn);
        
        headerPanel.add(searchPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Name", "Mobile", "Email", "Gender", "Height", "Weight", 
                           "Start Date", "End Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        membersTable = new JTable(tableModel);
        styleTable(membersTable);
        
        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(new Color(55, 55, 60));
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(new Color(55, 55, 60));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setForeground(new Color(220, 220, 220));
        table.setBackground(new Color(55, 55, 60));
        table.setSelectionBackground(new Color(85, 85, 90));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(70, 70, 75));
        table.setShowGrid(true);
        table.setOpaque(true);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(new Color(200, 200, 200));
        header.setBackground(new Color(50, 50, 50));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setReorderingAllowed(false);
        header.setOpaque(true);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(55, 55, 60));
        centerRenderer.setForeground(new Color(220, 220, 220));
        table.setDefaultRenderer(Object.class, centerRenderer);
    }
    
    private JButton createGlassButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(80, 80, 80, 180));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(100, 100, 100, 160));
                } else {
                    g2d.setColor(new Color(70, 70, 70, 140));
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(new Color(150, 150, 150, 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(new Color(220, 220, 220));
        button.setPreferredSize(new Dimension(140, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadMembers() {
        tableModel.setRowCount(0);
        
        try {
            Connection con = ConnectionProvider.getCon();
            String searchText = searchField.getText().trim();
            
            String query = "SELECT m.member_id, m.first_name, m.last_name, m.mobile, m.email, " +
                          "m.gender, m.height, m.weight, m.start_date, m.end_date, m.is_active " +
                          "FROM members m " +
                          "WHERE m.assigned_coach = ? " +
                          "AND (m.first_name LIKE ? OR m.last_name LIKE ? OR m.mobile LIKE ?) " +
                          "ORDER BY m.first_name";
            
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, currentCoach.getUserId());
            String searchPattern = "%" + searchText + "%";
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            
            ResultSet rs = ps.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            
            while (rs.next()) {
                int id = rs.getInt("member_id");
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                String mobile = rs.getString("mobile");
                String email = rs.getString("email");
                String gender = rs.getString("gender");
                double height = rs.getDouble("height");
                double weight = rs.getDouble("weight");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                boolean isActive = rs.getBoolean("is_active");
                
                String status;
                if (!isActive) {
                    status = "Inactive";
                } else if (endDate.isBefore(LocalDate.now())) {
                    status = "Expired";
                } else {
                    status = "Active";
                }
                
                tableModel.addRow(new Object[]{
                    id, name, mobile, email, gender, 
                    String.format("%.1f cm", height),
                    String.format("%.1f kg", weight),
                    startDate.format(formatter),
                    endDate.format(formatter),
                    status
                });
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading members: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatistics() {
        try {
            Connection con = ConnectionProvider.getCon();
            
            // Total members
            PreparedStatement ps1 = con.prepareStatement(
                "SELECT COUNT(*) FROM members WHERE assigned_coach = ?");
            ps1.setInt(1, currentCoach.getUserId());
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                totalLabel.setText(String.valueOf(rs1.getInt(1)));
            }
            rs1.close();
            ps1.close();
            
            // Active members
            PreparedStatement ps2 = con.prepareStatement(
                "SELECT COUNT(*) FROM members WHERE assigned_coach = ? AND end_date >= CURDATE() AND is_active = 1");
            ps2.setInt(1, currentCoach.getUserId());
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                activeLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            rs2.close();
            ps2.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
