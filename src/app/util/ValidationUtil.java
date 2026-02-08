package app.util;

import java.util.regex.Pattern;

/**
 * ValidationUtil - Input validation utilities
 * Provides validation methods for user inputs
 */
public class ValidationUtil {
    
    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "^01[0-2|5][0-9]{8}$" // Egyptian mobile format
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,50}$"
    );
    
    /**
     * Validate email address
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "Invalid email format");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Normalize mobile number by removing spaces, dashes, and converting +20 to 0
     */
    public static String normalizeMobile(String mobile) {
        if (mobile == null) {
            return null;
        }
        
        // Remove all spaces, dashes, parentheses
        mobile = mobile.replaceAll("[\\s\\-()]+", "");
        
        // Convert +20 prefix to 0
        if (mobile.startsWith("+20")) {
            mobile = "0" + mobile.substring(3);
        }
        
        // Convert 20 prefix (without +) to 0
        if (mobile.startsWith("20") && mobile.length() == 13) {
            mobile = "0" + mobile.substring(2);
        }
        
        return mobile;
    }
    
    /**
     * Validate mobile number (Egyptian format)
     */
    public static ValidationResult validateMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return new ValidationResult(false, "Mobile number is required");
        }
        
        // Normalize the mobile number first
        String normalizedMobile = normalizeMobile(mobile);
        
        if (!MOBILE_PATTERN.matcher(normalizedMobile).matches()) {
            return new ValidationResult(false, 
                "Invalid mobile format. Must be 11 digits starting with 01");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Simple mobile validation (returns boolean)
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        // Normalize before checking
        String normalizedMobile = normalizeMobile(mobile);
        return MOBILE_PATTERN.matcher(normalizedMobile).matches();
    }
    
    /**
     * Validate username
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "Username is required");
        }
        
        if (username.length() < 3) {
            return new ValidationResult(false, "Username must be at least 3 characters");
        }
        
        if (username.length() > 50) {
            return new ValidationResult(false, "Username must be less than 50 characters");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, 
                "Username can only contain letters, numbers, and underscore");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Validate name (first name or last name)
     */
    public static ValidationResult validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " is required");
        }
        
        if (name.length() < 2) {
            return new ValidationResult(false, fieldName + " must be at least 2 characters");
        }
        
        if (name.length() > 100) {
            return new ValidationResult(false, fieldName + " must be less than 100 characters");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Validate positive number
     */
    public static ValidationResult validatePositiveNumber(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " is required");
        }
        
        try {
            double num = Double.parseDouble(value);
            if (num <= 0) {
                return new ValidationResult(false, fieldName + " must be positive");
            }
        } catch (NumberFormatException e) {
            return new ValidationResult(false, fieldName + " must be a valid number");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Validate required field
     */
    public static ValidationResult validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " is required");
        }
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * ValidationResult class
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String message;
        
        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
        
        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
    }
}
