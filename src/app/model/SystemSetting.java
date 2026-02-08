package app.model;

import java.time.LocalDateTime;

/**
 * SystemSetting Model - Represents system-wide configuration settings
 * Maps to: system_settings table
 * 
 * This model stores key-value pairs for system configuration such as:
 * - Email settings (SMTP server, port, credentials)
 * - Business rules (membership pricing, late fee policies)
 * - UI preferences (theme, language, date format)
 * - System features (enable/disable modules)
 * 
 * Immutable DTO for system settings data
 */
public class SystemSetting {
    private final int settingId;
    private final String settingKey;
    private final String settingValue;
    private final String description;
    private final Integer updatedBy; // User ID who last updated
    private final LocalDateTime updatedAt;

    /**
     * Constructor for new settings (without ID)
     * @param settingKey Unique key identifier
     * @param settingValue Configuration value
     * @param description Human-readable description
     */
    public SystemSetting(String settingKey, String settingValue, String description) {
        this(0, settingKey, settingValue, description, null, null);
    }

    /**
     * Full constructor (from database)
     * @param settingId Primary key
     * @param settingKey Unique key identifier
     * @param settingValue Configuration value
     * @param description Human-readable description
     * @param updatedBy User ID who last updated
     * @param updatedAt Timestamp of last update
     */
    public SystemSetting(int settingId, String settingKey, String settingValue,
                        String description, Integer updatedBy, LocalDateTime updatedAt) {
        this.settingId = settingId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getSettingId() { 
        return settingId; 
    }

    public int getId() { 
        return settingId; 
    }

    public String getSettingKey() { 
        return settingKey; 
    }

    public String getKey() { 
        return settingKey; 
    }

    public String getSettingValue() { 
        return settingValue; 
    }

    public String getValue() { 
        return settingValue; 
    }

    public String getDescription() { 
        return description; 
    }

    public Integer getUpdatedBy() { 
        return updatedBy; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }

    // Helper methods for common value types
    
    /**
     * Parse setting value as integer
     * @return Integer value or null if parsing fails
     */
    public Integer getValueAsInt() {
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse setting value as boolean
     * Accepts: "true", "yes", "1", "enabled", "on" (case insensitive)
     * @return Boolean value
     */
    public boolean getValueAsBoolean() {
        if (settingValue == null) return false;
        String lower = settingValue.toLowerCase().trim();
        return lower.equals("true") || lower.equals("yes") || 
               lower.equals("1") || lower.equals("enabled") || lower.equals("on");
    }

    /**
     * Parse setting value as double
     * @return Double value or null if parsing fails
     */
    public Double getValueAsDouble() {
        try {
            return Double.parseDouble(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Immutable update methods
    
    /**
     * Create new instance with updated value
     * @param newValue New setting value
     * @param userId User ID performing the update
     * @return New SystemSetting instance
     */
    public SystemSetting withValue(String newValue, Integer userId) {
        return new SystemSetting(settingId, settingKey, newValue, description,
                                userId, LocalDateTime.now());
    }

    /**
     * Create new instance with updated description
     * @param newDescription New description
     * @return New SystemSetting instance
     */
    public SystemSetting withDescription(String newDescription) {
        return new SystemSetting(settingId, settingKey, settingValue, newDescription,
                                updatedBy, updatedAt);
    }

    /**
     * Create new instance with ID (after database insert)
     * @param newId Database-generated ID
     * @return New SystemSetting instance
     */
    public SystemSetting withId(int newId) {
        return new SystemSetting(newId, settingKey, settingValue, description,
                                updatedBy, updatedAt);
    }

    @Override
    public String toString() {
        return settingKey + "=" + settingValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SystemSetting that = (SystemSetting) obj;
        return settingKey != null && settingKey.equals(that.settingKey);
    }

    @Override
    public int hashCode() {
        return settingKey != null ? settingKey.hashCode() : 0;
    }

    /**
     * Common system setting keys as constants
     */
    public static class Keys {
        // Email Configuration
        public static final String SMTP_HOST = "smtp.host";
        public static final String SMTP_PORT = "smtp.port";
        public static final String SMTP_USERNAME = "smtp.username";
        public static final String SMTP_PASSWORD = "smtp.password";
        public static final String SMTP_USE_TLS = "smtp.use_tls";
        public static final String EMAIL_FROM = "email.from";
        
        // Business Rules
        public static final String DEFAULT_MEMBERSHIP_PRICE = "membership.default_price";
        public static final String LATE_FEE_AMOUNT = "membership.late_fee";
        public static final String TRIAL_PERIOD_DAYS = "membership.trial_days";
        public static final String AUTO_RENEWAL_ENABLED = "membership.auto_renewal";
        
        // System Settings
        public static final String SYSTEM_LANGUAGE = "system.language";
        public static final String SYSTEM_TIMEZONE = "system.timezone";
        public static final String DATE_FORMAT = "system.date_format";
        public static final String CURRENCY = "system.currency";
        
        // Feature Flags
        public static final String FEATURE_EMAIL_ENABLED = "feature.email_enabled";
        public static final String FEATURE_SMS_ENABLED = "feature.sms_enabled";
        public static final String FEATURE_REPORTS_ENABLED = "feature.reports_enabled";
        
        // Security
        public static final String PASSWORD_MIN_LENGTH = "security.password_min_length";
        public static final String SESSION_TIMEOUT_MINUTES = "security.session_timeout";
        public static final String MAX_LOGIN_ATTEMPTS = "security.max_login_attempts";
    }
}
