package app.model;

/**
 * UserRole Enum - System user roles
 */
public enum UserRole {
    OWNER("owner", "System Owner"),
    ADMIN("admin", "Branch Administrator"),
    COACH("coach", "Fitness Coach");

    private final String dbValue;
    private final String displayName;

    UserRole(String dbValue, String displayName) {
        this.dbValue = dbValue;
        this.displayName = displayName;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.dbValue.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
