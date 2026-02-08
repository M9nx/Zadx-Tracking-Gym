package app.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * EmailService - Handles sending emails via SMTP
 * Uses Gmail SMTP for email delivery
 * Configuration loaded from src/config/smtp.properties
 */
public class EmailService {
    
    private final Properties smtpProperties;
    private final boolean enabled;
    private final boolean dryRun;
    private final String dryRunPath;
    
    /**
     * Constructor - loads configuration
     */
    public EmailService() {
        this.smtpProperties = loadSmtpConfiguration();
        this.enabled = Boolean.parseBoolean(
            smtpProperties.getProperty("smtp.enabled", "false")
        );
        this.dryRun = Boolean.parseBoolean(
            smtpProperties.getProperty("smtp.dryrun", "true")
        );
        this.dryRunPath = smtpProperties.getProperty("smtp.dryrun.path", "logs/emails/");
    }
    
    /**
     * Load SMTP configuration from properties file
     */
    private Properties loadSmtpConfiguration() {
        Properties props = new Properties();
        
        try {
            // Try multiple locations for config file
            java.io.InputStream input = null;
            
            // 1. Try classpath (when running from JAR)
            input = getClass().getResourceAsStream("/config/smtp.properties");
            
            // 2. Try file system path (when running in IDE)
            if (input == null) {
                try {
                    input = new java.io.FileInputStream("src/config/smtp.properties");
                } catch (java.io.FileNotFoundException e) {
                    // Try alternate path
                    try {
                        input = new java.io.FileInputStream("config/smtp.properties");
                    } catch (java.io.FileNotFoundException e2) {
                        // Will handle below
                    }
                }
            }
            
            if (input != null) {
                props.load(input);
                input.close();
                System.out.println("✓ SMTP configuration loaded successfully");
                System.out.println("  - Enabled: " + props.getProperty("smtp.enabled"));
                System.out.println("  - Dry-run: " + props.getProperty("smtp.dryrun"));
                System.out.println("  - Host: " + props.getProperty("smtp.host"));
                System.out.println("  - Username: " + props.getProperty("smtp.username"));
            } else {
                // Use default configuration (dry-run mode)
                props.setProperty("smtp.enabled", "false");
                props.setProperty("smtp.dryrun", "true");
                props.setProperty("smtp.dryrun.path", "logs/emails/");
                System.out.println("⚠ SMTP configuration file not found - using dry-run mode");
                System.out.println("  To enable email: Create src/config/smtp.properties");
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading SMTP configuration: " + e.getMessage());
            e.printStackTrace();
            // Set safe defaults
            props.setProperty("smtp.enabled", "false");
            props.setProperty("smtp.dryrun", "true");
        }
        
        return props;
    }
    
    /**
     * Send email
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (plain text or HTML)
     * @param isHtml true if body is HTML, false for plain text
     * @return true if sent successfully, false otherwise
     */
    public boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        System.out.println("=== EmailService.sendEmail() called ===");
        System.out.println("  To: " + to);
        System.out.println("  Subject: " + subject);
        System.out.println("  Enabled: " + enabled);
        System.out.println("  Dry-run: " + dryRun);
        
        if (!enabled) {
            System.out.println("⚠ Email sending is disabled in configuration");
            if (dryRun) {
                return saveDryRunEmail(to, subject, body);
            }
            return false;
        }
        
        if (dryRun) {
            System.out.println("⚠ Dry-run mode: Email will be saved to file");
            return saveDryRunEmail(to, subject, body);
        }
        
        System.out.println("✓ Attempting to send real email via SMTP...");
        
        try {
            // Setup mail session
            Session session = Session.getInstance(getMailProperties(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        smtpProperties.getProperty("smtp.username"),
                        smtpProperties.getProperty("smtp.password")
                    );
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpProperties.getProperty("smtp.from")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            
            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }
            
            // Send
            System.out.println("  Sending email...");
            Transport.send(message);
            System.out.println("✓ Email sent successfully to: " + to);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("✗ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            // Fallback to dry-run
            System.out.println("  Falling back to dry-run mode...");
            return saveDryRunEmail(to, subject, body);
        }
    }
    
    /**
     * Send plain text email
     */
    public boolean sendEmail(String to, String subject, String body) {
        return sendEmail(to, subject, body, false);
    }
    
    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String to, String username, String newPassword) {
        String subject = "Your Password Has Been Reset - Gym Management System";
        
        String body = String.format(
            "Hello %s,\n\n" +
            "Your password has been reset by the system administrator.\n\n" +
            "Your new temporary password is: %s\n\n" +
            "IMPORTANT: Please change this password immediately after logging in.\n\n" +
            "If you did not request this password reset, please contact the system owner immediately.\n\n" +
            "Best regards,\n" +
            "Gym Management System\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            username, newPassword
        );
        
        return sendEmail(to, subject, body, false);
    }
    
    /**
     * Send owner forgot password email (overloaded with username and password)
     */
    public boolean sendOwnerForgotPasswordEmail(String recipientEmail, String username, String newPassword) {
        String subject = "OWNER Password Reset - Gym Management System";
        
        String body = String.format(
            "Hello %s,\n\n" +
            "A password reset was requested for the OWNER account.\n\n" +
            "Your new password is: %s\n\n" +
            "IMPORTANT SECURITY NOTICE:\n" +
            "- Change this password immediately after logging in\n" +
            "- Never share this password with anyone\n" +
            "- This password was auto-generated for security\n\n" +
            "If you did not request this reset, contact your system administrator immediately.\n\n" +
            "Timestamp: %s\n\n" +
            "Best regards,\n" +
            "Gym Management System\n\n" +
            "---\n" +
            "This is an automated security message.",
            username, newPassword,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        return sendEmail(recipientEmail, subject, body, false);
    }
    
    /**
     * Send owner forgot password email (simple version)
     */
    public boolean sendOwnerForgotPasswordEmail(String newPassword) {
        String ownerEmail = "m9nx11@gmail.com";
        String subject = "OWNER Password Reset - Gym Management System";
        
        String body = String.format(
            "System Owner,\n\n" +
            "A password reset was requested for the OWNER account.\n\n" +
            "Your new password is: %s\n\n" +
            "IMPORTANT SECURITY NOTICE:\n" +
            "- Change this password immediately after logging in\n" +
            "- Never share this password with anyone\n" +
            "- This password was auto-generated for security\n\n" +
            "If you did not request this reset, contact your system administrator immediately.\n\n" +
            "Timestamp: %s\n\n" +
            "Best regards,\n" +
            "Gym Management System\n\n" +
            "---\n" +
            "This is an automated security message.",
            newPassword,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        return sendEmail(ownerEmail, subject, body, false);
    }
    
    /**
     * Save email to file (dry-run mode)
     */
    private boolean saveDryRunEmail(String to, String subject, String body) {
        try {
            // Create directory if not exists
            Files.createDirectories(Paths.get(dryRunPath));
            
            // Generate filename
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            );
            String filename = dryRunPath + "email_" + timestamp + ".txt";
            
            // Write email content
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write("=== EMAIL (DRY-RUN MODE) ===\n");
                writer.write("Timestamp: " + LocalDateTime.now() + "\n");
                writer.write("To: " + to + "\n");
                writer.write("Subject: " + subject + "\n");
                writer.write("---\n");
                writer.write(body);
                writer.write("\n===========================\n");
            }
            
            System.out.println("✓ Dry-run email saved to: " + filename);
            return true;
            
        } catch (IOException e) {
            System.err.println("✗ Failed to save dry-run email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get mail properties for JavaMail
     */
    private Properties getMailProperties() {
        // Force IPv4 to fix DNS resolution issues
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", smtpProperties.getProperty("smtp.auth", "true"));
        props.put("mail.smtp.host", smtpProperties.getProperty("smtp.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", smtpProperties.getProperty("smtp.port", "587"));
        
        // Check if using SSL (port 465) or TLS (port 587)
        String useSsl = smtpProperties.getProperty("smtp.ssl", "false");
        String useStartTls = smtpProperties.getProperty("smtp.starttls", "true");
        
        if ("true".equalsIgnoreCase(useSsl)) {
            // SSL Configuration (port 465)
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", smtpProperties.getProperty("smtp.host", "smtp.gmail.com"));
            props.put("mail.smtp.socketFactory.port", smtpProperties.getProperty("smtp.port", "465"));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            // TLS Configuration (port 587)
            props.put("mail.smtp.starttls.enable", useStartTls);
            props.put("mail.smtp.ssl.trust", smtpProperties.getProperty("smtp.host", "smtp.gmail.com"));
        }
        
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        
        return props;
    }
    
    /**
     * Check if email service is properly configured
     */
    public boolean isConfigured() {
        return enabled && 
               smtpProperties.containsKey("smtp.host") &&
               smtpProperties.containsKey("smtp.username") &&
               smtpProperties.containsKey("smtp.password");
    }
    
    /**
     * Check if in dry-run mode
     */
    public boolean isDryRun() {
        return dryRun;
    }
    
    /**
     * Main method for testing email service
     */
    public static void main(String[] args) {
        System.out.println("=== Email Service Test ===\n");
        
        EmailService emailService = new EmailService();
        
        System.out.println("Enabled: " + emailService.enabled);
        System.out.println("Dry-run: " + emailService.dryRun);
        System.out.println("Configured: " + emailService.isConfigured());
        System.out.println();
        
        // Test sending email
        boolean result = emailService.sendEmail(
            "test@example.com",
            "Test Email",
            "This is a test email from Gym Management System."
        );
        
        System.out.println("\nResult: " + (result ? "SUCCESS" : "FAILED"));
    }
}
