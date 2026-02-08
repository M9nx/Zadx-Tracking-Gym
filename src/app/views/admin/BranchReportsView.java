package app.views.admin;

import Project.ConnectionProvider;
import app.model.User;
import app.util.SessionManager;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Branch Reports Panel - Shows statistics for admin's assigned branch
 * Integrated into AdminDashboard with blur background
 * 
 * Features:
 * - Shows only admin's branch data
 * - Total members, revenue, coaches
 * - Active vs inactive members
 * - Simple gray/red/green color scheme
 * - Transparent blur background
 */
public class BranchReportsView extends JPanel {
    
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    private JLabel totalMembersLabel;
    private JLabel totalRevenueLabel;
    private JLabel totalCoachesLabel;
    private JLabel totalActiveLabel;
    private final NumberFormat currencyFormat;
    private final Integer adminBranchId;
    private final String adminBranchName;
    
    public BranchReportsView() {
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        // Get current admin's branch
        User currentUser = SessionManager.getInstance().getCurrentUser();
        this.adminBranchId = currentUser != null ? currentUser.getBranchId() : null;
        this.adminBranchName = getBranchName(adminBranchId);
        
        setOpaque(false);
        initComponents();
        loadBranchStatistics();
    }
    
    private String getBranchName(Integer branchId) {
        if (branchId == null) return "All Branches";
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT branch_name FROM branches WHERE branch_id = ?");
            ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("branch_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Branch";
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Main panel with transparent blur background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent blur background
                g2d.setColor(new Color(40, 40, 40, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Statistics Cards Panel
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Bottom buttons panel
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel( adminBranchName + " - Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Branch performance analytics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, 5));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(subtitleLabel);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout(20, 20));
        statsPanel.setOpaque(false);
        
        // Summary cards at top
        JPanel summaryPanel = createSummaryCards();
        statsPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // Table in center
        JPanel tablePanel = createTablePanel();
        statsPanel.add(tablePanel, BorderLayout.CENTER);
        
        return statsPanel;
    }
    
    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 140));
        
        totalMembersLabel = new JLabel("0");
        totalActiveLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("$0.00");
        totalCoachesLabel = new JLabel("0");
        
        panel.add(createStatCard("Total Members", totalMembersLabel, new Color(150, 150, 150)));
        panel.add(createStatCard("Active Members", totalActiveLabel, new Color(46, 204, 113)));
        panel.add(createStatCard("Total Revenue", totalRevenueLabel, new Color(150, 150, 150)));
        panel.add(createStatCard("Coaches", totalCoachesLabel, new Color(150, 150, 150)));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Blur gray background
                g2d.setColor(new Color(60, 60, 60, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Simple border
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
                
                // Gray blur background
                g2d.setColor(new Color(50, 50, 50, 160));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Simple gray border
                g2d.setColor(new Color(100, 100, 100, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Table header label
        JLabel tableTitle = new JLabel("Detailed Statistics");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(new Color(200, 200, 200));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create table
        String[] columns = {
            "Metric", "Value", "Status"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportsTable = new JTable(tableModel);
        reportsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reportsTable.setRowHeight(35);
        reportsTable.setForeground(new Color(220, 220, 220));
        reportsTable.setBackground(new Color(55, 55, 60));
        reportsTable.setSelectionBackground(new Color(85, 85, 90));
        reportsTable.setSelectionForeground(Color.WHITE);
        reportsTable.setGridColor(new Color(70, 70, 75));
        reportsTable.setShowGrid(true);
        reportsTable.setIntercellSpacing(new Dimension(1, 1));
        reportsTable.setOpaque(true);
        
        // Table header styling
        JTableHeader header = reportsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(new Color(200, 200, 200));
        header.setBackground(new Color(50, 50, 50));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setReorderingAllowed(false);
        header.setOpaque(true);
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(reportsTable);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(new Color(55, 55, 60));
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(new Color(55, 55, 60));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        
        panel.add(tableTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);
        
        JButton refreshBtn = createGlassButton("Refresh", new Color(120, 120, 120));
        JButton exportBtn = createGlassButton("Export", new Color(120, 120, 120));
        
        refreshBtn.addActionListener(e -> {
            loadBranchStatistics();
            SwingUtilities.invokeLater(() -> {
                revalidate();
                repaint();
            });
        });
        
        exportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Export feature coming soon!", 
                "Export", JOptionPane.INFORMATION_MESSAGE);
        });
        
        panel.add(refreshBtn);
        panel.add(exportBtn);
        
        return panel;
    }
    
    private JButton createGlassButton(String text, Color accentColor) {
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
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(220, 220, 220));
        button.setPreferredSize(new Dimension(160, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadBranchStatistics() {
        if (adminBranchId == null) {
            JOptionPane.showMessageDialog(this, "No branch assigned to your account!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Connection con = null;
        PreparedStatement memberStmt = null;
        PreparedStatement coachStmt = null;
        ResultSet memberRs = null;
        ResultSet coachRs = null;
        
        try {
            con = ConnectionProvider.getCon();
            
            // Clear table before loading new data
            tableModel.setRowCount(0);
            
            int branchId = adminBranchId;
            String branchName = adminBranchName;
            
            // Get member count for this branch
            String memberQuery = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN end_date >= CURDATE() THEN 1 ELSE 0 END) as active, " +
                "SUM(CASE WHEN end_date < CURDATE() THEN 1 ELSE 0 END) as inactive, " +
                "SUM(payment) as revenue, " +
                "AVG(payment) as avg_payment " +
                "FROM members WHERE assigned_branch = ?";
            memberStmt = con.prepareStatement(memberQuery);
            memberStmt.setInt(1, branchId);
            memberRs = memberStmt.executeQuery();
            
            int memberCount = 0;
            int activeCount = 0;
            int inactiveCount = 0;
            double revenue = 0.0;
            double avgPayment = 0.0;
            
            if (memberRs.next()) {
                memberCount = memberRs.getInt("total");
                activeCount = memberRs.getInt("active");
                inactiveCount = memberRs.getInt("inactive");
                revenue = memberRs.getDouble("revenue");
                avgPayment = memberRs.getDouble("avg_payment");
            }
            
            // Get coach count for this branch
            String coachQuery = "SELECT COUNT(*) as total FROM users WHERE role = 'coach' AND branch_id = ?";
            coachStmt = con.prepareStatement(coachQuery);
            coachStmt.setInt(1, branchId);
            coachRs = coachStmt.executeQuery();
            
            int coachCount = 0;
            if (coachRs.next()) {
                coachCount = coachRs.getInt("total");
            }
            
            // Update summary cards
            totalMembersLabel.setText(String.valueOf(memberCount));
            totalActiveLabel.setText(String.valueOf(activeCount));
            totalRevenueLabel.setText(currencyFormat.format(revenue));
            totalCoachesLabel.setText(String.valueOf(coachCount));
            
            // Populate table with metrics
            String activeStatus = activeCount > 0 ? "✅ Good" : "⚠️ Low";
            String inactiveStatus = inactiveCount > 10 ? "⚠️ High" : "✅ Normal";
            
            tableModel.addRow(new Object[]{"Total Members", memberCount, ""});
            tableModel.addRow(new Object[]{"Active Members", activeCount, activeStatus});
            tableModel.addRow(new Object[]{"Inactive Members", inactiveCount, inactiveStatus});
            tableModel.addRow(new Object[]{"Total Coaches", coachCount, ""});
            tableModel.addRow(new Object[]{"Total Revenue", currencyFormat.format(revenue), ""});
            tableModel.addRow(new Object[]{"Average Payment", currencyFormat.format(avgPayment), ""});
            
            System.out.println("✅ " + branchName + " statistics loaded");
            System.out.println("   Members: " + memberCount + " (" + activeCount + " active)");
            System.out.println("   Revenue: " + currencyFormat.format(revenue));
            System.out.println("   Coaches: " + coachCount);
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading branch statistics: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading branch statistics:\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close all resources
            try {
                if (memberRs != null) memberRs.close();
                if (coachRs != null) coachRs.close();
                if (memberStmt != null) memberStmt.close();
                if (coachStmt != null) coachStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

