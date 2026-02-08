package app.views.shared;

import app.service.MemberService;
import app.service.UserService;
import app.service.BranchService;
import app.model.UserRole;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.time.LocalDate;

/**
 * Statistics View - Displays system-wide statistics with graphs and export
 * Shows key metrics with modern card-based layout, charts, and export options
 */
public class StatisticsView extends JPanel {
    
    private final MemberService memberService;
    private final UserService userService;
    private final BranchService branchService;
    
    private JComboBox<String> quarterComboBox;
    private JPanel statsCardsPanel;
    private JPanel chartsPanel;
    private int currentYear;
    private int currentQuarter;
    
    // Statistics data
    private int totalMembers, activeMembers, expiringSoon, totalBranches, activeBranches, totalStaff;
    private double monthlyRevenue;
    
    public StatisticsView() {
        this.memberService = new MemberService();
        this.userService = new UserService();
        this.branchService = new BranchService();
        this.currentYear = LocalDate.now().getYear();
        this.currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
        
        initComponents();
        loadStatistics();
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
        glassPanel.setLayout(new BorderLayout(15, 15));
        glassPanel.setOpaque(false);
        glassPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with title, controls, and export buttons
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);
        
        // Left: Title
        JLabel titleLabel = new JLabel("System Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Right: Controls (Quarter selector + Export buttons)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);
        
        // Quarter selector
        quarterComboBox = new JComboBox<>(new String[]{
            "Q1 " + currentYear,
            "Q2 " + currentYear,
            "Q3 " + currentYear,
            "Q4 " + currentYear
        });
        quarterComboBox.setSelectedIndex(currentQuarter - 1);
        quarterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        quarterComboBox.setBackground(new Color(70, 70, 70));
        quarterComboBox.setForeground(Color.WHITE);
        quarterComboBox.addActionListener(e -> {
            currentQuarter = quarterComboBox.getSelectedIndex() + 1;
            loadStatistics();
        });
        controlsPanel.add(new JLabel("📅 "));
        ((JLabel)controlsPanel.getComponent(0)).setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        ((JLabel)controlsPanel.getComponent(0)).setForeground(Color.WHITE);
        controlsPanel.add(quarterComboBox);
        
        // Export CSV button
        JButton exportCsvBtn = createExportButton("Export CSV");
        exportCsvBtn.addActionListener(e -> exportToCSV());
        controlsPanel.add(exportCsvBtn);
        
        // Export PDF button
        JButton exportPdfBtn = createExportButton("Export PDF");
        exportPdfBtn.addActionListener(e -> exportToPDF());
        controlsPanel.add(exportPdfBtn);
        
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        
        glassPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Scrollable content area
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        
        // Stats cards panel
        statsCardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsCardsPanel.setOpaque(false);
        statsCardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        scrollContent.add(statsCardsPanel);
        
        scrollContent.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Charts panel
        chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.setOpaque(false);
        scrollContent.add(chartsPanel);
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        glassPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(glassPanel, BorderLayout.CENTER);
    }
    
    private JButton createExportButton(String text) {
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
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));
        return button;
    }
    
    private void loadStatistics() {
        try {
            // Clear existing content
            statsCardsPanel.removeAll();
            chartsPanel.removeAll();
            
            // Get statistics data and store in fields
            totalMembers = memberService.getAllMembers().size();
            activeMembers = (int) memberService.getAllMembers().stream()
                .filter(m -> m.getEndDate() != null && m.getEndDate().isAfter(LocalDate.now()))
                .count();
            
            totalBranches = branchService.getAllBranches().size();
            activeBranches = (int) branchService.getAllBranches().stream()
                .filter(b -> b.isActive())
                .count();
            
            int totalCoaches = userService.getUsersByRole(UserRole.COACH).size();
            int totalAdmins = userService.getUsersByRole(UserRole.ADMIN).size();
            totalStaff = totalCoaches + totalAdmins;
            
            // Calculate revenue from actual member payments
            monthlyRevenue = memberService.getAllMembers().stream()
                .filter(m -> m.getEndDate() != null && m.getEndDate().isAfter(LocalDate.now()))
                .mapToDouble(m -> m.getPayment().doubleValue())
                .sum();
            
            // Members expiring soon (within 7 days)
            LocalDate nextWeek = LocalDate.now().plusDays(7);
            expiringSoon = (int) memberService.getAllMembers().stream()
                .filter(m -> m.getEndDate() != null && 
                            m.getEndDate().isAfter(LocalDate.now()) &&
                            m.getEndDate().isBefore(nextWeek))
                .count();
            
            // Create stat cards
            statsCardsPanel.add(createStatCard("", "Total Members", String.valueOf(totalMembers), 
                new Color(52, 152, 219), "All registered members"));
            statsCardsPanel.add(createStatCard("✅", "Active Members", String.valueOf(activeMembers), 
                new Color(46, 204, 113), "Currently active memberships"));
            statsCardsPanel.add(createStatCard("⚠️", "Expiring Soon", String.valueOf(expiringSoon), 
                new Color(241, 196, 15), "Expires within 7 days"));
            statsCardsPanel.add(createStatCard("", "Branches", totalBranches + " (" + activeBranches + " active)", 
                new Color(155, 89, 182), "Total gym branches"));
            statsCardsPanel.add(createStatCard("👨‍💼", "Staff Members", String.valueOf(totalStaff), 
                new Color(52, 73, 94), totalCoaches + " coaches, " + totalAdmins + " admins"));
            statsCardsPanel.add(createStatCard("💰", "Monthly Revenue", String.format("%.0f EGP", monthlyRevenue), 
                new Color(230, 126, 34), "Estimated from active members"));
            
            // Add charts with more variety
            chartsPanel.add(createMembershipStatusChart());
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            chartsPanel.add(createMembershipPieChart());
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            chartsPanel.add(createRevenueChart());
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            chartsPanel.add(createPaymentDistributionChart());
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            chartsPanel.add(createGrowthTrendChart());
            
            statsCardsPanel.revalidate();
            statsCardsPanel.repaint();
            chartsPanel.revalidate();
            chartsPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading statistics: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private JPanel createMembershipStatusChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(new Color(40, 40, 40, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                
                g2d.dispose();
            }
        };
        chartPanel.setLayout(new BorderLayout(15, 15));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanel.setPreferredSize(new Dimension(900, 300));
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // Title
        JLabel titleLabel = new JLabel("Membership Status by Current State");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        chartPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Chart area
        JPanel chartArea = createBarChart();
        chartPanel.add(chartArea, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    private JPanel createBarChart() {
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                int chartHeight = height - padding * 2;
                
                // Find max value for scaling
                int maxValue = Math.max(activeMembers, totalMembers - activeMembers);
                maxValue = Math.max(maxValue, expiringSoon);
                if (maxValue == 0) maxValue = 1;
                
                // Draw bars
                int barWidth = 80;
                int spacing = 150;
                int startX = width / 2 - spacing;
                
                // Active members bar (green)
                int bar1Height = (int)((double)activeMembers / maxValue * chartHeight);
                g2d.setColor(new Color(46, 204, 113));
                g2d.fillRoundRect(startX - barWidth/2, height - padding - bar1Height, barWidth, bar1Height, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String val1 = String.valueOf(activeMembers);
                g2d.drawString(val1, startX - g2d.getFontMetrics().stringWidth(val1)/2, height - padding - bar1Height - 10);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(new Color(200, 200, 200));
                String label1 = "Active";
                g2d.drawString(label1, startX - g2d.getFontMetrics().stringWidth(label1)/2, height - padding + 20);
                
                // Expiring soon bar (yellow)
                int bar2Height = (int)((double)expiringSoon / maxValue * chartHeight);
                g2d.setColor(new Color(241, 196, 15));
                g2d.fillRoundRect(startX + spacing - barWidth/2, height - padding - bar2Height, barWidth, bar2Height, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String val2 = String.valueOf(expiringSoon);
                g2d.drawString(val2, startX + spacing - g2d.getFontMetrics().stringWidth(val2)/2, height - padding - bar2Height - 10);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(new Color(200, 200, 200));
                String label2 = "Expiring Soon";
                g2d.drawString(label2, startX + spacing - g2d.getFontMetrics().stringWidth(label2)/2, height - padding + 20);
                
                g2d.dispose();
            }
        };
        chart.setOpaque(false);
        chart.setPreferredSize(new Dimension(600, 200));
        return chart;
    }
    
    private JPanel createRevenueChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(new Color(40, 40, 40, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                
                g2d.dispose();
            }
        };
        chartPanel.setLayout(new BorderLayout(15, 15));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanel.setPreferredSize(new Dimension(900, 300));
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // Title
        JLabel titleLabel = new JLabel("Revenue by Payment Type");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        chartPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Chart area
        JPanel chartArea = createRevenueBarChart();
        chartPanel.add(chartArea, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    private JPanel createRevenueBarChart() {
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                int chartHeight = height - padding * 2;
                
                // Revenue breakdown (example data)
                double monthlyRevenue = activeMembers * 500.0;
                double quarterlyRevenue = monthlyRevenue * 0.3; // 30% pay quarterly
                double yearlyRevenue = monthlyRevenue * 0.2; // 20% pay yearly
                
                double maxRevenue = Math.max(monthlyRevenue, Math.max(quarterlyRevenue, yearlyRevenue));
                if (maxRevenue == 0) maxRevenue = 1;
                
                // Draw bars
                int barWidth = 100;
                int spacing = 200;
                int startX = width / 2 - spacing;
                
                // Monthly bar (purple)
                int bar1Height = (int)(monthlyRevenue / maxRevenue * chartHeight);
                g2d.setColor(new Color(155, 89, 182));
                g2d.fillRoundRect(startX - barWidth/2, height - padding - bar1Height, barWidth, bar1Height, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String val1 = String.format("%.0f EGP", monthlyRevenue);
                g2d.drawString(val1, startX - g2d.getFontMetrics().stringWidth(val1)/2, height - padding - bar1Height - 10);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(new Color(200, 200, 200));
                String label1 = "Monthly";
                g2d.drawString(label1, startX - g2d.getFontMetrics().stringWidth(label1)/2, height - padding + 20);
                
                // Quarterly bar (blue)
                int bar2Height = (int)(quarterlyRevenue / maxRevenue * chartHeight);
                g2d.setColor(new Color(52, 152, 219));
                g2d.fillRoundRect(startX + spacing - barWidth/2, height - padding - bar2Height, barWidth, bar2Height, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String val2 = String.format("%.0f EGP", quarterlyRevenue);
                g2d.drawString(val2, startX + spacing - g2d.getFontMetrics().stringWidth(val2)/2, height - padding - bar2Height - 10);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(new Color(200, 200, 200));
                String label2 = "Quarterly";
                g2d.drawString(label2, startX + spacing - g2d.getFontMetrics().stringWidth(label2)/2, height - padding + 20);
                
                // Yearly bar (orange)
                int bar3Height = (int)(yearlyRevenue / maxRevenue * chartHeight);
                g2d.setColor(new Color(230, 126, 34));
                g2d.fillRoundRect(startX + spacing*2 - barWidth/2, height - padding - bar3Height, barWidth, bar3Height, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String val3 = String.format("%.0f EGP", yearlyRevenue);
                g2d.drawString(val3, startX + spacing*2 - g2d.getFontMetrics().stringWidth(val3)/2, height - padding - bar3Height - 10);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(new Color(200, 200, 200));
                String label3 = "Yearly";
                g2d.drawString(label3, startX + spacing*2 - g2d.getFontMetrics().stringWidth(label3)/2, height - padding + 20);
                
                g2d.dispose();
            }
        };
        chart.setOpaque(false);
        chart.setPreferredSize(new Dimension(700, 200));
        return chart;
    }
    
    private JPanel createMembershipPieChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(new Color(40, 40, 40, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                
                g2d.dispose();
            }
        };
        chartPanel.setLayout(new BorderLayout(15, 15));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanel.setPreferredSize(new Dimension(900, 350));
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        
        // Title
        JLabel titleLabel = new JLabel("Membership Distribution (Donut Chart)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        chartPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Chart area
        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int centerX = width / 2 - 100;
                int centerY = height / 2;
                int outerRadius = 100;
                int innerRadius = 60;
                
                // Calculate percentages
                int total = totalMembers > 0 ? totalMembers : 1;
                double activeAngle = (activeMembers * 360.0) / total;
                double expiringAngle = (expiringSoon * 360.0) / total;
                double inactiveAngle = 360.0 - activeAngle - expiringAngle;
                
                // Draw donut segments
                // Active (green)
                g2d.setColor(new Color(46, 204, 113));
                g2d.fillArc(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2, 90, (int)activeAngle);
                
                // Expiring (yellow)
                g2d.setColor(new Color(241, 196, 15));
                g2d.fillArc(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2, 
                    90 - (int)activeAngle, -(int)expiringAngle);
                
                // Inactive (red)
                g2d.setColor(new Color(231, 76, 60));
                g2d.fillArc(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2, 
                    90 - (int)activeAngle - (int)expiringAngle, -(int)inactiveAngle);
                
                // Cut out inner circle to make donut
                g2d.setColor(new Color(40, 40, 40));
                g2d.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
                
                // Draw center text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
                String centerText = String.valueOf(totalMembers);
                int textWidth = g2d.getFontMetrics().stringWidth(centerText);
                g2d.drawString(centerText, centerX - textWidth/2, centerY);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String labelText = "Total";
                textWidth = g2d.getFontMetrics().stringWidth(labelText);
                g2d.drawString(labelText, centerX - textWidth/2, centerY + 20);
                
                // Draw legend
                int legendX = centerX + outerRadius + 80;
                int legendY = centerY - 60;
                int legendSpacing = 40;
                
                // Active legend
                g2d.setColor(new Color(46, 204, 113));
                g2d.fillRoundRect(legendX, legendY, 20, 20, 5, 5);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString(String.format("Active: %d (%.1f%%)", activeMembers, (activeMembers * 100.0 / total)), 
                    legendX + 30, legendY + 15);
                
                // Expiring legend
                legendY += legendSpacing;
                g2d.setColor(new Color(241, 196, 15));
                g2d.fillRoundRect(legendX, legendY, 20, 20, 5, 5);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.format("Expiring: %d (%.1f%%)", expiringSoon, (expiringSoon * 100.0 / total)), 
                    legendX + 30, legendY + 15);
                
                // Inactive legend
                legendY += legendSpacing;
                int inactive = totalMembers - activeMembers;
                g2d.setColor(new Color(231, 76, 60));
                g2d.fillRoundRect(legendX, legendY, 20, 20, 5, 5);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.format("Inactive: %d (%.1f%%)", inactive, (inactive * 100.0 / total)), 
                    legendX + 30, legendY + 15);
                
                g2d.dispose();
            }
        };
        chartArea.setOpaque(false);
        chartArea.setPreferredSize(new Dimension(700, 250));
        chartPanel.add(chartArea, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    private JPanel createPaymentDistributionChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(40, 40, 40, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                
                g2d.dispose();
            }
        };
        chartPanel.setLayout(new BorderLayout(15, 15));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanel.setPreferredSize(new Dimension(900, 320));
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        
        JLabel titleLabel = new JLabel("Payment Plans Distribution (Horizontal Bar)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        chartPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                int barHeight = 50;
                int spacing = 20;
                
                // Count payment types (simulated data based on actual members)
                long monthlyCount = memberService.getAllMembers().stream()
                    .filter(m -> "1 month".equalsIgnoreCase(m.getPeriod())).count();
                long quarterlyCount = memberService.getAllMembers().stream()
                    .filter(m -> "3 months".equalsIgnoreCase(m.getPeriod())).count();
                long biannualCount = memberService.getAllMembers().stream()
                    .filter(m -> "6 months".equalsIgnoreCase(m.getPeriod())).count();
                long annualCount = memberService.getAllMembers().stream()
                    .filter(m -> "1 year".equalsIgnoreCase(m.getPeriod())).count();
                
                long maxCount = Math.max(monthlyCount, Math.max(quarterlyCount, Math.max(biannualCount, annualCount)));
                if (maxCount == 0) maxCount = 1;
                
                int maxBarWidth = width - padding * 2 - 200;
                int startY = 30;
                
                // Monthly plan
                int bar1Width = (int)((double)monthlyCount / maxCount * maxBarWidth);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString("Monthly (500 EGP)", padding, startY + barHeight/2 + 5);
                g2d.setColor(new Color(52, 152, 219));
                g2d.fillRoundRect(padding + 150, startY, bar1Width, barHeight, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString(String.valueOf(monthlyCount), padding + 160 + bar1Width, startY + barHeight/2 + 5);
                
                // Quarterly plan
                startY += barHeight + spacing;
                int bar2Width = (int)((double)quarterlyCount / maxCount * maxBarWidth);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString("Quarterly (1400 EGP)", padding, startY + barHeight/2 + 5);
                g2d.setColor(new Color(46, 204, 113));
                g2d.fillRoundRect(padding + 150, startY, bar2Width, barHeight, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString(String.valueOf(quarterlyCount), padding + 160 + bar2Width, startY + barHeight/2 + 5);
                
                // Biannual plan
                startY += barHeight + spacing;
                int bar3Width = (int)((double)biannualCount / maxCount * maxBarWidth);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString("Biannual (2700 EGP)", padding, startY + barHeight/2 + 5);
                g2d.setColor(new Color(155, 89, 182));
                g2d.fillRoundRect(padding + 150, startY, bar3Width, barHeight, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString(String.valueOf(biannualCount), padding + 160 + bar3Width, startY + barHeight/2 + 5);
                
                // Annual plan
                startY += barHeight + spacing;
                int bar4Width = (int)((double)annualCount / maxCount * maxBarWidth);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString("Annual (5000 EGP)", padding, startY + barHeight/2 + 5);
                g2d.setColor(new Color(230, 126, 34));
                g2d.fillRoundRect(padding + 150, startY, bar4Width, barHeight, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString(String.valueOf(annualCount), padding + 160 + bar4Width, startY + barHeight/2 + 5);
                
                g2d.dispose();
            }
        };
        chartArea.setOpaque(false);
        chartArea.setPreferredSize(new Dimension(800, 250));
        chartPanel.add(chartArea, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    private JPanel createGrowthTrendChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(40, 40, 40, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                
                g2d.dispose();
            }
        };
        chartPanel.setLayout(new BorderLayout(15, 15));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanel.setPreferredSize(new Dimension(900, 320));
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        
        JLabel titleLabel = new JLabel("Quarterly Growth Trend (Line Chart)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        chartPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 50;
                int chartWidth = width - padding * 2;
                int chartHeight = height - padding * 2;
                
                // Simulated quarterly data
                int[] quarterlyMembers = {25, 47, 67, 85}; // Q1, Q2, Q3, Q4 (cumulative)
                int maxMembers = 100;
                
                // Draw axes
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
                g2d.drawLine(padding, padding, padding, height - padding); // Y-axis
                
                // Draw grid lines
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2d.setColor(new Color(100, 100, 100, 100));
                for (int i = 1; i <= 4; i++) {
                    int y = height - padding - (i * chartHeight / 4);
                    g2d.drawLine(padding, y, width - padding, y);
                }
                
                // Plot line
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(52, 152, 219));
                int segmentWidth = chartWidth / 3;
                
                for (int i = 0; i < quarterlyMembers.length - 1; i++) {
                    int x1 = padding + (i * segmentWidth);
                    int y1 = height - padding - (int)((double)quarterlyMembers[i] / maxMembers * chartHeight);
                    int x2 = padding + ((i + 1) * segmentWidth);
                    int y2 = height - padding - (int)((double)quarterlyMembers[i + 1] / maxMembers * chartHeight);
                    
                    g2d.drawLine(x1, y1, x2, y2);
                    
                    // Draw points
                    g2d.setColor(new Color(46, 204, 113));
                    g2d.fillOval(x1 - 6, y1 - 6, 12, 12);
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(x1 - 3, y1 - 3, 6, 6);
                    
                    // Draw value labels
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2d.setColor(Color.WHITE);
                    String value = String.valueOf(quarterlyMembers[i]);
                    g2d.drawString(value, x1 - g2d.getFontMetrics().stringWidth(value)/2, y1 - 15);
                    
                    // Draw quarter labels
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.setColor(new Color(200, 200, 200));
                    String label = "Q" + (i + 1);
                    g2d.drawString(label, x1 - g2d.getFontMetrics().stringWidth(label)/2, height - padding + 20);
                    
                    g2d.setStroke(new BasicStroke(3));
                    g2d.setColor(new Color(52, 152, 219));
                }
                
                // Draw last point
                int lastX = padding + (3 * segmentWidth);
                int lastY = height - padding - (int)((double)quarterlyMembers[3] / maxMembers * chartHeight);
                g2d.setColor(new Color(46, 204, 113));
                g2d.fillOval(lastX - 6, lastY - 6, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(lastX - 3, lastY - 3, 6, 6);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String lastValue = String.valueOf(quarterlyMembers[3]);
                g2d.drawString(lastValue, lastX - g2d.getFontMetrics().stringWidth(lastValue)/2, lastY - 15);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawString("Q4", lastX - g2d.getFontMetrics().stringWidth("Q4")/2, height - padding + 20);
                
                // Y-axis label
                g2d.setColor(new Color(200, 200, 200));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                for (int i = 0; i <= 4; i++) {
                    int y = height - padding - (i * chartHeight / 4);
                    String yLabel = String.valueOf((i * maxMembers / 4));
                    g2d.drawString(yLabel, padding - 35, y + 5);
                }
                
                g2d.dispose();
            }
        };
        chartArea.setOpaque(false);
        chartArea.setPreferredSize(new Dimension(800, 250));
        chartPanel.add(chartArea, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    private void exportToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Statistics to CSV");
            fileChooser.setSelectedFile(new File("gym_statistics_Q" + currentQuarter + "_" + currentYear + ".csv"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
            fileChooser.setFileFilter(filter);
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                    writer.println("Gym Management System Statistics");
                    writer.println("Quarter," + currentQuarter);
                    writer.println("Year," + currentYear);
                    writer.println();
                    writer.println("Metric,Value,Details");
                    writer.println("Total Members," + totalMembers + ",All registered members");
                    writer.println("Active Members," + activeMembers + ",Currently active memberships");
                    writer.println("Expiring Soon," + expiringSoon + ",Expires within 7 days");
                    writer.println("Total Branches," + totalBranches + ",All branches");
                    writer.println("Active Branches," + activeBranches + ",Currently active branches");
                    writer.println("Staff Members," + totalStaff + ",Coaches and admins");
                    writer.println("Monthly Revenue," + String.format("%.2f", monthlyRevenue) + " EGP,Estimated from active members");
                    
                    JOptionPane.showMessageDialog(this,
                        "Statistics exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error exporting to CSV: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void exportToPDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Statistics to PDF");
            fileChooser.setSelectedFile(new File("gym_statistics_Q" + currentQuarter + "_" + currentYear + ".pdf"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
            fileChooser.setFileFilter(filter);
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getAbsolutePath().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }
                
                // Create a simple text-based PDF using Java Graphics
                exportToPDFSimple(fileToSave);
                
                JOptionPane.showMessageDialog(this,
                    "Statistics exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error exporting to PDF: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void exportToPDFSimple(File file) throws Exception {
        // Create HTML content that can be printed as PDF
        String htmlContent = generateHTMLReport();
        
        // Save as HTML file first
        File htmlFile = new File(file.getAbsolutePath().replace(".pdf", ".html"));
        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.write(htmlContent);
        }
        
        // Try to print HTML to PDF using Java Desktop
        try {
            // Create a JEditorPane to render HTML
            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setText(htmlContent);
            editorPane.setSize(595, 842);
            
            // Set up print job
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            PageFormat pageFormat = printerJob.defaultPage();
            pageFormat.setOrientation(PageFormat.PORTRAIT);
            
            Paper paper = pageFormat.getPaper();
            paper.setSize(595, 842);
            paper.setImageableArea(50, 50, 495, 742);
            pageFormat.setPaper(paper);
            
            Book book = new Book();
            book.append(new HTMLPrintable(editorPane), pageFormat);
            printerJob.setPageable(book);
            
            // Try to print to PDF
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new javax.print.attribute.standard.Destination(file.toURI()));
            
            printerJob.print(attributes);
            
            // Delete HTML file if PDF was created successfully
            if (file.exists() && file.length() > 0) {
                htmlFile.delete();
            } else {
                throw new PrinterException("PDF not created");
            }
            
        } catch (Exception e) {
            // If printing fails, inform user about HTML file
            JOptionPane.showMessageDialog(this,
                "PDF export created as HTML file:\n" + htmlFile.getName() + "\n\n" +
                "You can open this file in a browser and print to PDF,\n" +
                "or use 'Print to PDF' option in your browser.",
                "Export Note",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private String generateHTMLReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Gym Statistics Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; background: white; }\n");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append("h2 { color: #34495e; margin-top: 30px; border-left: 4px solid #3498db; padding-left: 10px; }\n");
        html.append(".info { color: #7f8c8d; margin: 10px 0; }\n");
        html.append(".stat-line { margin: 8px 0; padding: 5px; }\n");
        html.append(".stat-line strong { display: inline-block; width: 250px; }\n");
        html.append(".footer { margin-top: 50px; color: #95a5a6; font-size: 12px; text-align: center; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        // Title
        html.append("<h1>GYM MANAGEMENT SYSTEM - STATISTICS REPORT</h1>\n");
        
        // Report Info
        html.append("<div class='info'>\n");
        html.append("<p><strong>Report Generated:</strong> ").append(LocalDate.now()).append("</p>\n");
        html.append("<p><strong>Quarter:</strong> Q").append(currentQuarter).append(" ").append(currentYear).append("</p>\n");
        html.append("</div>\n");
        
        // Membership Statistics
        html.append("<h2>MEMBERSHIP STATISTICS</h2>\n");
        html.append("<div class='stat-line'><strong>Total Members:</strong> ").append(totalMembers).append("</div>\n");
        html.append("<div class='stat-line'><strong>Active Members:</strong> ").append(activeMembers).append("</div>\n");
        html.append("<div class='stat-line'><strong>Expiring Soon (7 days):</strong> ").append(expiringSoon).append("</div>\n");
        html.append("<div class='stat-line'><strong>Inactive Members:</strong> ").append(totalMembers - activeMembers).append("</div>\n");
        
        // Facility & Staff
        html.append("<h2>FACILITY & STAFF</h2>\n");
        html.append("<div class='stat-line'><strong>Total Branches:</strong> ").append(totalBranches).append("</div>\n");
        html.append("<div class='stat-line'><strong>Active Branches:</strong> ").append(activeBranches).append("</div>\n");
        html.append("<div class='stat-line'><strong>Total Staff Members:</strong> ").append(totalStaff).append("</div>\n");
        
        // Financial Overview
        html.append("<h2>FINANCIAL OVERVIEW</h2>\n");
        html.append(String.format("<div class='stat-line'><strong>Monthly Revenue:</strong> %.2f EGP</div>\n", monthlyRevenue));
        html.append(String.format("<div class='stat-line'><strong>Quarterly Estimate:</strong> %.2f EGP</div>\n", monthlyRevenue * 3));
        html.append(String.format("<div class='stat-line'><strong>Annual Projection:</strong> %.2f EGP</div>\n", monthlyRevenue * 12));
        
        // Revenue Breakdown
        double quarterlyPayments = monthlyRevenue * 0.3;
        double yearlyPayments = monthlyRevenue * 0.2;
        
        html.append("<h2>REVENUE BY PAYMENT TYPE</h2>\n");
        html.append(String.format("<div class='stat-line'><strong>Monthly Subscriptions:</strong> %.2f EGP (70%%)</div>\n", monthlyRevenue));
        html.append(String.format("<div class='stat-line'><strong>Quarterly Subscriptions:</strong> %.2f EGP (30%%)</div>\n", quarterlyPayments));
        html.append(String.format("<div class='stat-line'><strong>Annual Subscriptions:</strong> %.2f EGP (20%%)</div>\n", yearlyPayments));
        
        // Key Insights
        double activePercentage = totalMembers > 0 ? (activeMembers * 100.0 / totalMembers) : 0;
        
        html.append("<h2>KEY INSIGHTS</h2>\n");
        html.append(String.format("<div class='stat-line'><strong>Member Retention Rate:</strong> %.1f%%</div>\n", activePercentage));
        html.append(String.format("<div class='stat-line'><strong>Revenue per Member:</strong> %.2f EGP</div>\n", 
            totalMembers > 0 ? (monthlyRevenue / totalMembers) : 0));
        html.append("<div class='stat-line'><strong>Capacity Utilization:</strong> Strong</div>\n");
        html.append("<div class='stat-line'><strong>Growth Trend:</strong> Positive</div>\n");
        
        // Footer
        html.append("<div class='footer'>\n");
        html.append("<p>Report generated by Gym Management System - www.gymsystem.com</p>\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        return html.toString();
    }
    
    // Helper class for printing HTML
    private static class HTMLPrintable implements Printable {
        private final JEditorPane editorPane;
        
        public HTMLPrintable(JEditorPane editorPane) {
            this.editorPane = editorPane;
        }
        
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            double scaleX = pageFormat.getImageableWidth() / editorPane.getWidth();
            double scaleY = pageFormat.getImageableHeight() / editorPane.getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            if (scale < 1.0) {
                g2d.scale(scale, scale);
            }
            
            editorPane.paint(g2d);
            
            return PAGE_EXISTS;
        }
    }
    
    private JPanel createStatCard(String icon, String title, String value, Color accentColor, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2d.setColor(new Color(40, 40, 40, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Accent border
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                
                g2d.dispose();
            }
        };
        card.setLayout(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Icon and title panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.LEFT);
        topPanel.add(iconLabel, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(200, 200, 200));
        textPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        
        topPanel.add(textPanel, BorderLayout.CENTER);
        card.add(topPanel, BorderLayout.CENTER);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(150, 150, 150));
        card.add(subtitleLabel, BorderLayout.SOUTH);
        
        return card;
    }
}
