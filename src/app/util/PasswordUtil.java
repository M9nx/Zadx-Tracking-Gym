package app.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordUtil - Secure password hashing and validation
 * Uses PBKDF2 with SHA-256 for password hashing
 * 
 * For BCrypt alternative, add spring-security-crypto or jBCrypt dependency
 */
public class PasswordUtil {
    
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    
    // Password complexity requirements
    private static final int MIN_LENGTH = 10;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    
    /**
     * Hash a password using PBKDF2
     * @param password Plain text password
     * @return Hashed password in format: salt:hash (Base64 encoded)
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            
            // Return salt:hash encoded in Base64
            return Base64.getEncoder().encodeToString(salt) + ":" + 
                   Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a hashed password
     * @param password Plain text password to verify
     * @param storedHash Stored hash from database (salt:hash format)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the input password with the same salt
            byte[] testHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            
            // Compare hashes in constant time to prevent timing attacks
            return slowEquals(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate password complexity
     * @param password Password to validate
     * @return ValidationResult with isValid and error message
     */
    public static ValidationUtil.ValidationResult validatePasswordComplexity(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return new ValidationUtil.ValidationResult(false, 
                "Password must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return new ValidationUtil.ValidationResult(false, 
                "Password must contain at least one uppercase letter");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return new ValidationUtil.ValidationResult(false, 
                "Password must contain at least one lowercase letter");
        }
        
        if (!DIGIT_PATTERN.matcher(password).find()) {
            return new ValidationUtil.ValidationResult(false, 
                "Password must contain at least one digit");
        }
        
        if (!SPECIAL_PATTERN.matcher(password).find()) {
            return new ValidationUtil.ValidationResult(false, 
                "Password must contain at least one special character");
        }
        
        return new ValidationUtil.ValidationResult(true, "Password is valid");
    }
    
    /**
     * Generate a secure random password
     * @param length Length of password to generate
     * @return Randomly generated password meeting complexity requirements
     */
    public static String generateSecurePassword(int length) {
        if (length < MIN_LENGTH) {
            length = MIN_LENGTH;
        }
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}";
        String allChars = uppercase + lowercase + digits + special;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one of each required character type
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * PBKDF2 key derivation function
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
    
    /**
     * Constant-time comparison to prevent timing attacks
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
    

    
    /**
     * Main method for testing password hashing
     * Run this to generate hashed passwords for SQL seed data
     */
    public static void main(String[] args) {
        System.out.println("=== Password Hashing Utility ===\n");
        
        // Generate hashes for seed data
        String[] passwords = {"owner123", "admin123", "coach123"};
        
        for (String password : passwords) {
            String hash = hashPassword(password);
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println("Verification: " + verifyPassword(password, hash));
            System.out.println();
        }
        
        // Generate secure random password
        System.out.println("Generated secure password: " + generateSecurePassword(12));
        
        // Test validation
        System.out.println("\n=== Password Validation Tests ===");
        String[] testPasswords = {"weak", "StrongPass123!", "NoSpecial123"};
        for (String pwd : testPasswords) {
            ValidationUtil.ValidationResult result = validatePasswordComplexity(pwd);
            System.out.println("Password: " + pwd);
            System.out.println("Valid: " + result.isValid() + " - " + result.getMessage());
            System.out.println();
        }
    }
}
