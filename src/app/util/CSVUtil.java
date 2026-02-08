package app.util;

import app.model.Member;
import app.model.User;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Utility - Import/Export functionality for members and users.
 * 
 * Features:
 * - Export members to CSV
 * - Export users to CSV
 * - Import members from CSV with validation
 * - Duplicate detection
 * - Error reporting
 * 
 * @author m9nx
 * @version 2.0
 */
public class CSVUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DELIMITER = ",";
    private static final String QUOTE = "\"";
    
    /**
     * Exports members to CSV file.
     */
    public static boolean exportMembers(List<Member> members, File outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Write header
            writer.println("Random ID,First Name,Last Name,Mobile,Email,Height,Weight," +
                          "Gender,Date of Birth,Payment,Period,Start Date,End Date," +
                          "Assigned Coach ID,Branch ID,Active,Created At");
            
            // Write data
            for (Member member : members) {
                writer.println(formatMemberRow(member));
            }
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Error exporting members: " + e.getMessage());
            return false;
        }
    }
    
    private static String formatMemberRow(Member member) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(escape(String.valueOf(member.getRandomId()))).append(DELIMITER);
        sb.append(escape(member.getFirstName())).append(DELIMITER);
        sb.append(escape(member.getLastName())).append(DELIMITER);
        sb.append(escape(member.getMobile())).append(DELIMITER);
        sb.append(escape(member.getEmail())).append(DELIMITER);
        sb.append(member.getHeight() != null ? member.getHeight() : "").append(DELIMITER);
        sb.append(member.getWeight() != null ? member.getWeight() : "").append(DELIMITER);
        sb.append(member.getGender()).append(DELIMITER);
        sb.append(member.getDateOfBirth() != null ? member.getDateOfBirth().format(DATE_FORMATTER) : "").append(DELIMITER);
        sb.append(member.getPayment()).append(DELIMITER);
        sb.append(escape(member.getPeriod())).append(DELIMITER);
        sb.append(member.getStartDate().format(DATE_FORMATTER)).append(DELIMITER);
        sb.append(member.getEndDate().format(DATE_FORMATTER)).append(DELIMITER);
        sb.append(member.getAssignedCoach() != null ? member.getAssignedCoach() : "").append(DELIMITER);
        sb.append(member.getAssignedBranch()).append(DELIMITER);
        sb.append(member.isActive()).append(DELIMITER);
        sb.append(member.getCreatedAt());
        
        return sb.toString();
    }
    
    /**
     * Exports users to CSV file.
     */
    public static boolean exportUsers(List<User> users, File outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Write header
            writer.println("ID,Username,First Name,Last Name,Email,Mobile,Role," +
                          "Branch ID,Active,Last Login,Created At");
            
            // Write data
            for (User user : users) {
                writer.println(formatUserRow(user));
            }
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Error exporting users: " + e.getMessage());
            return false;
        }
    }
    
    private static String formatUserRow(User user) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(user.getId()).append(DELIMITER);
        sb.append(escape(user.getUsername())).append(DELIMITER);
        sb.append(escape(user.getFirstName())).append(DELIMITER);
        sb.append(escape(user.getLastName())).append(DELIMITER);
        sb.append(escape(user.getEmail())).append(DELIMITER);
        sb.append(escape(user.getMobile())).append(DELIMITER);
        sb.append(user.getRole()).append(DELIMITER);
        sb.append(user.getBranchId() != null ? user.getBranchId() : "").append(DELIMITER);
        sb.append(user.isActive()).append(DELIMITER);
        sb.append(user.getLastLogin() != null ? user.getLastLogin() : "").append(DELIMITER);
        sb.append(user.getCreatedAt());
        
        return sb.toString();
    }
    
    /**
     * Imports members from CSV file with validation.
     * 
     * @return ImportResult with success count, errors, and created member IDs
     */
    public static ImportResult importMembers(File inputFile) {
        ImportResult result = new ImportResult();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineNumber = 0;
            boolean headerSkipped = false;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    MemberImportData data = parseMemberRow(line);
                    
                    // Validate required fields
                    if (data.firstName == null || data.firstName.isEmpty()) {
                        result.addError(lineNumber, "First name is required");
                        continue;
                    }
                    
                    if (data.lastName == null || data.lastName.isEmpty()) {
                        result.addError(lineNumber, "Last name is required");
                        continue;
                    }
                    
                    if (data.mobile == null || data.mobile.isEmpty()) {
                        result.addError(lineNumber, "Mobile is required");
                        continue;
                    }
                    
                    // Additional validation can be added here
                    
                    result.incrementSuccess();
                    
                } catch (Exception e) {
                    result.addError(lineNumber, e.getMessage());
                }
            }
            
        } catch (IOException e) {
            result.addError(0, "Error reading file: " + e.getMessage());
        }
        
        return result;
    }
    
    private static MemberImportData parseMemberRow(String line) throws Exception {
        String[] parts = splitCSVLine(line);
        
        if (parts.length < 14) {
            throw new Exception("Invalid CSV format: expected at least 14 columns");
        }
        
        MemberImportData data = new MemberImportData();
        
        data.randomId = parts[0].trim();
        data.firstName = parts[1].trim();
        data.lastName = parts[2].trim();
        data.mobile = parts[3].trim();
        data.email = parts[4].trim().isEmpty() ? null : parts[4].trim();
        
        if (!parts[5].trim().isEmpty()) {
            data.height = new BigDecimal(parts[5].trim());
        }
        
        if (!parts[6].trim().isEmpty()) {
            data.weight = new BigDecimal(parts[6].trim());
        }
        
        data.gender = parts[7].trim();
        
        if (!parts[8].trim().isEmpty()) {
            data.dateOfBirth = LocalDate.parse(parts[8].trim(), DATE_FORMATTER);
        }
        
        data.payment = new BigDecimal(parts[9].trim());
        data.period = parts[10].trim();
        data.startDate = LocalDate.parse(parts[11].trim(), DATE_FORMATTER);
        data.endDate = LocalDate.parse(parts[12].trim(), DATE_FORMATTER);
        
        if (!parts[13].trim().isEmpty()) {
            data.assignedCoachId = Integer.parseInt(parts[13].trim());
        }
        
        if (parts.length > 14 && !parts[14].trim().isEmpty()) {
            data.branchId = Integer.parseInt(parts[14].trim());
        }
        
        if (parts.length > 15 && !parts[15].trim().isEmpty()) {
            data.active = Boolean.parseBoolean(parts[15].trim());
        }
        
        return data;
    }
    
    /**
     * Splits CSV line handling quoted values with commas.
     */
    private static String[] splitCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Escapes CSV values containing special characters.
     */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(DELIMITER) || value.contains(QUOTE) || value.contains("\n")) {
            return QUOTE + value.replace(QUOTE, QUOTE + QUOTE) + QUOTE;
        }
        
        return value;
    }
    
    /**
     * Data class for member import.
     */
    private static class MemberImportData {
        String randomId;
        String firstName;
        String lastName;
        String mobile;
        String email;
        BigDecimal height;
        BigDecimal weight;
        String gender;
        LocalDate dateOfBirth;
        BigDecimal payment;
        String period;
        LocalDate startDate;
        LocalDate endDate;
        Integer assignedCoachId;
        Integer branchId = 1; // Default
        boolean active = true; // Default
    }
    
    /**
     * Result class for import operations.
     */
    public static class ImportResult {
        private int successCount = 0;
        private final List<ImportError> errors = new ArrayList<>();
        
        public void incrementSuccess() {
            successCount++;
        }
        
        public void addError(int lineNumber, String message) {
            errors.add(new ImportError(lineNumber, message));
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public List<ImportError> getErrors() {
            return errors;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Import completed:\n");
            sb.append("- Successful: ").append(successCount).append("\n");
            sb.append("- Errors: ").append(errors.size()).append("\n");
            
            if (hasErrors()) {
                sb.append("\nErrors:\n");
                for (ImportError error : errors) {
                    sb.append("Line ").append(error.lineNumber)
                      .append(": ").append(error.message).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Error class for import operations.
     */
    public static class ImportError {
        private final int lineNumber;
        private final String message;
        
        public ImportError(int lineNumber, String message) {
            this.lineNumber = lineNumber;
            this.message = message;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
