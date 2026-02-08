package app.util;

import app.model.User;

/**
 * SessionManager - Manages user session state
 * Singleton pattern for current logged-in user
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private String ipAddress;
    
    private SessionManager() {
        // Private constructor
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Start a new session
     */
    public void startSession(User user, String ipAddress) {
        this.currentUser = user;
        this.ipAddress = ipAddress;
        System.out.println("✓ Session started for user: " + user.getUsername());
    }
    
    /**
     * End current session
     */
    public void endSession() {
        if (currentUser != null) {
            System.out.println("✓ Session ended for user: " + currentUser.getUsername());
            this.currentUser = null;
            this.ipAddress = null;
        }
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current user's IP address
     */
    public String getIpAddress() {
        return ipAddress != null ? ipAddress : "unknown";
    }
    
    /**
     * Check if current user is owner
     */
    public boolean isOwner() {
        return currentUser != null && currentUser.isOwner();
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Check if current user is coach
     */
    public boolean isCoach() {
        return currentUser != null && currentUser.isCoach();
    }
    
    /**
     * Get current user's branch ID (null for owner)
     */
    public Integer getCurrentBranchId() {
        return currentUser != null ? currentUser.getBranchId() : null;
    }
    
    /**
     * Check if user has access to a specific branch
     */
    public boolean hasAccessToBranch(int branchId) {
        if (currentUser == null) return false;
        if (currentUser.isOwner()) return true; // Owner has access to all branches
        return currentUser.getBranchId() != null && currentUser.getBranchId() == branchId;
    }
    
    /**
     * Require owner role (throws exception if not owner)
     */
    public void requireOwner() throws SecurityException {
        if (!isOwner()) {
            throw new SecurityException("This action requires OWNER privileges");
        }
    }
    
    /**
     * Require admin role or higher (throws exception if coach)
     */
    public void requireAdminOrHigher() throws SecurityException {
        if (!isAdmin() && !isOwner()) {
            throw new SecurityException("This action requires ADMIN or OWNER privileges");
        }
    }
}
