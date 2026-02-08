package app.views.coach;

import Project.ConnectionProvider;
import app.model.Member;
import app.model.User;
import app.dao.MemberDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Training Progress View for Coaches
 * Shows members assigned to coach and allows adding training notes
 * Features:
 * - View assigned members
 * - Add training session notes
 * - View training history
 * - Filter by member
 */
public class TrainingProgressView extends JPanel {
    
    private final User currentCoach;
    private final MemberDAO memberDAO;
    private JTable membersTable;
    private JTable progressTable;
    private DefaultTableModel membersTableModel;
    private DefaultTableModel progressTableModel;
    private JTextField searchField;
    private int selectedMemberId = -1;
    
    public TrainingProgressView(User coach) {
        this.currentCoach = coach;
        this.memberDAO = new MemberDAO();
        
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadMyMembers();
    }
    
    private void initComponents() {
        // Main container with blur background
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
        
        // Split panel for members and progress
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(5);
        
        // Top: My Members
        JPanel membersPanel = createMembersPanel();
        splitPane.setTopComponent(membersPanel);
        
        // Bottom: Training Progress History
        JPanel progressPanel = createProgressPanel();
        splitPane.setBottomComponent(progressPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Training Progress Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(220, 220, 220));
        
        JLabel subtitleLabel = new JLabel("Track training sessions for your assigned members");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createMembersPanel() {
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
        
        JLabel label = new JLabel("👥 My Assigned Members");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(200, 200, 200));
        headerPanel.add(label, BorderLayout.WEST);
        
        // Search field
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
                loadMyMembers();
            }
        });
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("🔍"));
        searchPanel.add(searchField);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Members table
        String[] columns = {"ID", "Member Name", "Mobile", "Membership Status", "End Date"};
        membersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        membersTable = new JTable(membersTableModel);
        styleTable(membersTable);
        
        membersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && membersTable.getSelectedRow() >= 0) {
                selectedMemberId = (int) membersTableModel.getValueAt(membersTable.getSelectedRow(), 0);
                loadTrainingHistory();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProgressPanel() {
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
        
        // Header with add button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel label = new JLabel("📝 Training History");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(200, 200, 200));
        headerPanel.add(label, BorderLayout.WEST);
        
        JButton addProgressBtn = createGlassButton("➕ Add Training Note");
        addProgressBtn.addActionListener(e -> addTrainingNote());
        headerPanel.add(addProgressBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Progress table
        String[] columns = {"Date", "Member", "Notes", "Rating"};
        progressTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        progressTable = new JTable(progressTableModel);
        styleTable(progressTable);
        
        JScrollPane scrollPane = new JScrollPane(progressTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
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
        button.setPreferredSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadMyMembers() {
        membersTableModel.setRowCount(0);
        
        try {
            Connection con = ConnectionProvider.getCon();
            String searchText = searchField != null ? searchField.getText().trim() : "";
            
            String query = "SELECT m.member_id, m.first_name, m.last_name, m.mobile, " +
                          "m.start_date, m.end_date, m.is_active " +
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
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            while (rs.next()) {
                int id = rs.getInt("member_id");
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                String mobile = rs.getString("mobile");
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                boolean isActive = rs.getBoolean("is_active");
                
                String status;
                if (!isActive) {
                    status = "❌ Inactive";
                } else if (endDate.isBefore(LocalDate.now())) {
                    status = "⚠️ Expired";
                } else {
                    status = "✅ Active";
                }
                
                membersTableModel.addRow(new Object[]{
                    id, name, mobile, status, endDate.format(formatter)
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
    
    private void loadTrainingHistory() {
        progressTableModel.setRowCount(0);
        
        if (selectedMemberId <= 0) return;
        
        try {
            Connection con = ConnectionProvider.getCon();
            
            String query = "SELECT tp.session_date, tp.notes, tp.rating, " +
                          "m.first_name, m.last_name " +
                          "FROM training_progress tp " +
                          "JOIN members m ON tp.member_id = m.member_id " +
                          "WHERE tp.member_id = ? " +
                          "ORDER BY tp.session_date DESC " +
                          "LIMIT 50";
            
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, selectedMemberId);
            
            ResultSet rs = ps.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            
            while (rs.next()) {
                LocalDate date = rs.getDate("session_date").toLocalDate();
                String memberName = rs.getString("first_name") + " " + rs.getString("last_name");
                String notes = rs.getString("notes");
                int rating = rs.getInt("rating");
                
                // Truncate notes if too long
                if (notes.length() > 80) {
                    notes = notes.substring(0, 77) + "...";
                }
                
                String ratingStr = "";
                for (int i = 0; i < rating; i++) {
                    ratingStr += "⭐";
                }
                
                progressTableModel.addRow(new Object[]{
                    date.format(formatter), memberName, notes, ratingStr
                });
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading training history: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addTrainingNote() {
        if (selectedMemberId <= 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a member first!",
                "No Member Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get member name
        String memberName = "";
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                "SELECT first_name, last_name FROM members WHERE member_id = ?");
            ps.setInt(1, selectedMemberId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                memberName = rs.getString("first_name") + " " + rs.getString("last_name");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                     "Add Training Note - " + memberName, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setBackground(new Color(35, 35, 40));
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Session Date
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel dateLabel = new JLabel("Session Date:");
        dateLabel.setForeground(Color.WHITE);
        panel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date());
        panel.add(dateSpinner, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JLabel notesLabel = new JLabel("Training Notes:");
        notesLabel.setForeground(Color.WHITE);
        panel.add(notesLabel, gbc);
        
        gbc.gridy = 2;
        JTextArea notesArea = new JTextArea(8, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(new Color(45, 45, 50));
        notesArea.setForeground(Color.WHITE);
        notesArea.setCaretColor(Color.WHITE);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 80)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        panel.add(notesScroll, gbc);
        
        // Rating
        gbc.gridy = 3; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel ratingLabel = new JLabel("Session Rating (1-5):");
        ratingLabel.setForeground(Color.WHITE);
        panel.add(ratingLabel, gbc);
        
        gbc.gridy = 4;
        JSlider ratingSlider = new JSlider(1, 5, 3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setBackground(new Color(35, 35, 40));
        ratingSlider.setForeground(Color.WHITE);
        panel.add(ratingSlider, gbc);
        
        // Buttons
        gbc.gridy = 5;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton saveBtn = new JButton("💾 Save");
        JButton cancelBtn = new JButton("❌ Cancel");
        
        saveBtn.addActionListener(e -> {
            String notes = notesArea.getText().trim();
            if (notes.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter training notes!");
                return;
            }
            
            try {
                Connection con = ConnectionProvider.getCon();
                String sql = "INSERT INTO training_progress (member_id, coach_id, session_date, notes, rating) " +
                            "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, selectedMemberId);
                ps.setInt(2, currentCoach.getUserId());
                
                java.util.Date date = (java.util.Date) dateSpinner.getValue();
                ps.setDate(3, new java.sql.Date(date.getTime()));
                ps.setString(4, notes);
                ps.setInt(5, ratingSlider.getValue());
                
                ps.executeUpdate();
                ps.close();
                
                JOptionPane.showMessageDialog(dialog, "Training note saved successfully!");
                dialog.dispose();
                loadTrainingHistory();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                    "Error saving training note: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}
