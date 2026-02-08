import java.sql.Connection;
import Project.ConnectionProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

public class LOGIN extends javax.swing.JFrame {


    public LOGIN() {
        initComponents();
        this.setLocationRelativeTo(null);
        // Make window responsive and scalable
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                revalidate();
                repaint();
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        t1 = new javax.swing.JTextField();
        t2 = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                try {
                    java.awt.Image img = new javax.swing.ImageIcon(getClass().getResource("/Images/ambitious-studio-rick-barrett-1RNQ11ZODJM-unsplash.jpg")).getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    setBackground(new java.awt.Color(0, 0, 0));
                }
            }
        };

        jLabel1.setBackground(new java.awt.Color(0, 0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 72)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("WELCOME");
        jLabel1.setOpaque(false);

        jPanel2 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                // Blur gray glass effect with more transparency
                g2d.setColor(new java.awt.Color(50, 50, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // Subtle border
                g2d.setColor(new java.awt.Color(255, 255, 255, 50));
                g2d.setStroke(new java.awt.BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 30, 30);
                g2d.dispose();
            }
        };
        jPanel2.setOpaque(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Username");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Password");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        t1 = new javax.swing.JTextField() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new java.awt.Color(255, 255, 255, 100));
                g2d.setStroke(new java.awt.BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
                g2d.dispose();
            }
        };
        t1.setFont(new java.awt.Font("Segoe UI", 0, 14));
        t1.setBackground(new java.awt.Color(255, 255, 255, 230));
        t1.setForeground(new java.awt.Color(30, 30, 30));
        t1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
        t1.setOpaque(false);
        t1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t2.requestFocus();
            }
        });

        t2 = new javax.swing.JPasswordField() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new java.awt.Color(255, 255, 255, 100));
                g2d.setStroke(new java.awt.BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
                g2d.dispose();
            }
        };
        t2.setFont(new java.awt.Font("Segoe UI", 0, 14));
        t2.setBackground(new java.awt.Color(255, 255, 255, 230));
        t2.setForeground(new java.awt.Color(30, 30, 30));
        t2.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
        t2.setOpaque(false);
        t2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t2ActionPerformed(evt);
            }
        });

        jButton1 = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new java.awt.Color(50, 50, 50));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new java.awt.Color(90, 90, 90));
                } else {
                    g2d.setColor(new java.awt.Color(70, 70, 70));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.setColor(new java.awt.Color(255, 255, 255, 100));
                g2d.setStroke(new java.awt.BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1.setText("LOGIN");
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setFocusPainted(false);
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addComponent(t1)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(t2)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(t1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(t2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("");

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton2.setText("Forgot password?");
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setBackground(new java.awt.Color(0, 0, 0, 0));
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton2.setForeground(new java.awt.Color(200, 200, 200));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton2.setForeground(new java.awt.Color(255, 255, 255));
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void t2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t2ActionPerformed
        // Trigger login when Enter is pressed in password field
        jButton1ActionPerformed(evt);
    }//GEN-LAST:event_t2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    String username = t1.getText().trim();
    String password = String.valueOf(t2.getPassword()).trim();

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter both username and password!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Connection con = ConnectionProvider.getCon();
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);
        
        // Try users table first (BCrypt hashed passwords)
        String query = "SELECT * FROM users WHERE username = ?";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, username);
        ResultSet rs = pst.executeQuery();
        
        boolean authenticated = false;
        app.model.User user = null;
        
        if (rs.next()) {
            System.out.println("User found in users table");
            // Verify BCrypt password
            String storedHash = rs.getString("password");
            System.out.println("Verifying password...");
            boolean passwordMatch = app.util.PasswordUtil.verifyPassword(password, storedHash);
            System.out.println("Password match: " + passwordMatch);
            
            if (passwordMatch) {
                authenticated = true;
                
                // Get user data
                int userId = rs.getInt("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String mobile = rs.getString("mobile");
                String role = rs.getString("role");
                Integer branchId = rs.getObject("branch_id", Integer.class);
                boolean isActive = rs.getBoolean("is_active");
                
                // Check if user is active
                if (!isActive) {
                    JOptionPane.showMessageDialog(this, "Your account has been deactivated. Please contact the administrator.", "Account Inactive", JOptionPane.WARNING_MESSAGE);
                    pst.close();
                    con.close();
                    return;
                }
                
                // Create User object
                app.model.UserRole userRole = app.model.UserRole.fromString(role);
                user = new app.model.User(
                    userId,
                    username,
                    storedHash,
                    firstName,
                    lastName,
                    email,
                    mobile,
                    userRole,
                    branchId,
                    isActive,
                    null,
                    null,
                    null
                );
                
                // Update last login
                String updateLogin = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
                PreparedStatement updateStmt = con.prepareStatement(updateLogin);
                updateStmt.setInt(1, userId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                // Set session
                app.util.SessionManager.getInstance().startSession(user, "127.0.0.1");
                
                System.out.println("Login successful: " + firstName + " " + lastName + " (" + role + ")");
            }
        } else {
            System.out.println("User NOT found in users table");
        }
        
        // If not found in users table, try regist table (legacy - plain text)
        if (!authenticated) {
            System.out.println("Trying regist table...");
            pst.close();
            pst.close();
            query = "SELECT * FROM regist WHERE username = ? AND password = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                authenticated = true;
                
                String role = rs.getString("role");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                int userId = rs.getInt("id");
                
                app.model.UserRole userRole = app.model.UserRole.fromString(role);
                user = new app.model.User(
                    userId,
                    username,
                    password,
                    firstName,
                    lastName,
                    username + "@gym.com",
                    "",
                    userRole,
                    null,
                    true,
                    null,
                    null,
                    null
                );
                
                System.out.println("Login successful (legacy): " + firstName + " " + lastName + " (" + role + ")");
            }
        }
        
        pst.close();
        con.close();
        
        if (authenticated && user != null) {
            // Route to appropriate dashboard
            this.dispose();
            
            if (user.getRole() == app.model.UserRole.OWNER) {
                new app.views.owner.OwnerDashboard(user).setVisible(true);
            } else if (user.getRole() == app.model.UserRole.ADMIN) {
                new app.views.admin.AdminDashboard(user).setVisible(true);
            } else if (user.getRole() == app.model.UserRole.COACH) {
                new app.views.coach.CoachDashboard(user).setVisible(true);
            } else {
                new Main_INTERFACE().setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        PasswordResetPage resetPage = new PasswordResetPage();
        resetPage.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LOGIN().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField t1;
    private javax.swing.JPasswordField t2;
    // End of variables declaration//GEN-END:variables
}

