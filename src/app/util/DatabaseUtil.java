package app.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * DatabaseUtil - Centralized database connection management
 * Singleton pattern with connection pooling support
 */
public class DatabaseUtil {
    
    private static DatabaseUtil instance;
    private String jdbcUrl;
    private String username;
    private String password;
    private boolean isConfigured = false;
    
    // Default configuration
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "gymm";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";
    
    /**
     * Private constructor - Singleton pattern
     */
    private DatabaseUtil() {
        loadConfiguration();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }
    
    /**
     * Load database configuration from properties file or use defaults
     */
    private void loadConfiguration() {
        try {
            // Try to load from config file
            Properties props = new Properties();
            InputStream input = DatabaseUtil.class.getResourceAsStream("/config/database.properties");
            
            if (input != null) {
                props.load(input);
                String host = props.getProperty("db.host", DEFAULT_HOST);
                String port = props.getProperty("db.port", DEFAULT_PORT);
                String database = props.getProperty("db.database", DEFAULT_DATABASE);
                this.username = props.getProperty("db.username", DEFAULT_USER);
                this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
                
                this.jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    host, port, database
                );
                input.close();
            } else {
                // Use default configuration
                useDefaultConfiguration();
            }
            
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            isConfigured = true;
            
        } catch (Exception e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            useDefaultConfiguration();
        }
    }
    
    /**
     * Use default configuration
     */
    private void useDefaultConfiguration() {
        this.jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE
        );
        this.username = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;
        isConfigured = true;
    }
    
    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (!isConfigured) {
            throw new SQLException("Database is not configured");
        }
        
        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("✓ Database connected successfully");
            return conn;
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            handleConnectionError(e);
            throw e;
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Handle connection errors with user-friendly messages
     */
    private void handleConnectionError(SQLException e) {
        String errorMsg = buildErrorMessage(e);
        System.err.println(errorMsg);
        
        // Show dialog in GUI context
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return; // Don't show GUI dialogs in headless mode (tests)
        }
        
        JOptionPane.showMessageDialog(null, errorMsg, 
            "Database Connection Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Build detailed error message
     */
    private String buildErrorMessage(SQLException e) {
        StringBuilder msg = new StringBuilder();
        msg.append("Cannot connect to database!\n\n");
        msg.append("Please check:\n");
        msg.append("1. MySQL Server is running\n");
        msg.append("2. Database '").append(DEFAULT_DATABASE).append("' exists\n");
        msg.append("3. Run sql/schema_and_seed.sql in MySQL Workbench\n");
        msg.append("4. Username: ").append(username).append("\n");
        msg.append("5. MySQL is on port ").append(DEFAULT_PORT).append("\n\n");
        msg.append("Error Details:\n");
        msg.append(e.getMessage());
        
        return msg.toString();
    }
    
    /**
     * Close connection safely
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✓ Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get JDBC URL
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    /**
     * Manual configuration (for testing or runtime updates)
     */
    public void configure(String host, String port, String database, 
                         String username, String password) {
        this.jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            host, port, database
        );
        this.username = username;
        this.password = password;
        this.isConfigured = true;
    }
    
    /**
     * Main method for testing database connection
     */
    public static void main(String[] args) {
        System.out.println("=== Database Connection Test ===\n");
        
        DatabaseUtil db = DatabaseUtil.getInstance();
        
        System.out.println("JDBC URL: " + db.getJdbcUrl());
        System.out.println("Testing connection...\n");
        
        if (db.testConnection()) {
            System.out.println("✓ SUCCESS: Database connection is working!");
        } else {
            System.out.println("✗ FAILED: Could not connect to database");
        }
    }
}
