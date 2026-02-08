package app.service;

import app.dao.AuditLogDAO;
import app.dao.MemberDAO;
import app.model.Member;
import app.model.Member.Gender;
import app.model.User;
import app.util.DateUtil;
import app.util.SessionManager;
import app.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Member Service - Business logic for member management operations.
 * 
 * This service handles:
 * - Member CRUD operations
 * - Random ID generation (8-digit unique)
 * - Membership period calculation
 * - Expiration tracking
 * - Coach assignment
 * - Search and filtering
 * - Audit logging for member operations
 * 
 * Business Rules:
 * - Random ID must be unique (8 digits)
 * - Mobile number must be unique
 * - Period is calculated from payment amount (150 LE per month)
 * - End date is auto-calculated from start date + period
 * - Coaches can only view/edit members assigned to them
 * - Admins can only manage members in their branch
 * - Owner can manage all members
 * 
 * @author Mounir 
 * @version 2.0
 */

public class MemberService {
    
    private final MemberDAO memberDAO;
    private final AuditLogDAO auditLogDAO;
    private final SessionManager sessionManager;
    private final Random random;
    
    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("150.00");
    
    /**
     * Constructs the MemberService with required dependencies.
     */
    public MemberService() {
        this.memberDAO = new MemberDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.sessionManager = SessionManager.getInstance();
        this.random = new Random();
    }
    
    /**
     * Creates a new member with validation and audit logging.
     * 
     * @param firstName the member's first name
     * @param lastName the member's last name
     * @param mobile the unique mobile number
     * @param email the email address (optional)
     * @param height the height in cm (optional)
     * @param weight the weight in kg (optional)
     * @param gender the gender (MALE or FEMALE)
     * @param dateOfBirth the date of birth
     * @param payment the payment amount in LE
     * @param startDate the membership start date
     * @param assignedCoach the ID of the assigned coach (optional)
     * @param assignedBranch the ID of the assigned branch
     * @param ipAddress the IP address for audit logging
     * @return the created Member object, or empty if creation failed
     */
    public Optional<Member> createMember(
            String firstName,
            String lastName,
            String mobile,
            String email,
            BigDecimal height,
            BigDecimal weight,
            Gender gender,
            LocalDate dateOfBirth,
            BigDecimal payment,
            LocalDate startDate,
            Integer assignedCoach,
            int assignedBranch,
            String ipAddress) {
        
        try {
            // Normalize mobile number (remove spaces, +20, etc.)
            mobile = ValidationUtil.normalizeMobile(mobile);
            
            // Validate inputs
            ValidationUtil.ValidationResult validation = validateMemberInputs(
                firstName, lastName, mobile, email, payment, startDate
            );
            
            if (!validation.isValid()) {
                System.err.println("Member creation validation failed: " + validation.getMessage());
                return Optional.empty();
            }
            
            // Generate unique random ID
            String randomId = generateUniqueRandomId();
            
            if (randomId == null) {
                System.err.println("Failed to generate unique random ID");
                return Optional.empty();
            }
            
            int randomIdInt = Integer.parseInt(randomId);
            
            // Calculate period from payment
            String period = DateUtil.calculatePeriod(payment, MONTHLY_PRICE);
            
            // Calculate end date
            LocalDate endDate = DateUtil.calculateEndDate(startDate, payment, MONTHLY_PRICE);
            
            // Create member
            Member newMember = new Member(
                randomIdInt,
                firstName,
                lastName,
                mobile,
                email,
                height,
                weight,
                gender,
                dateOfBirth,
                payment,
                startDate,
                endDate,
                period,
                assignedBranch,
                assignedCoach,
                "" // notes
            );
            
            boolean created = memberDAO.create(newMember);
            
            if (!created) {
                System.err.println("Failed to create member in database");
                return Optional.empty();
            }
            
            // Retrieve created member (to get auto-generated ID)
            Optional<Member> createdMember = memberDAO.findByRandomId(randomIdInt);
            
            if (createdMember.isPresent()) {
                // Log member creation
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "MEMBER_CREATE",
                    "Created new member: " + firstName + " " + lastName + " (ID: " + randomId + ")",
                    ipAddress
                );
            }
            
            return createdMember;
            
        } catch (Exception e) {
            System.err.println("Error creating member: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Updates an existing member with validation and audit logging.
     * 
     * @param memberId the ID of the member to update
     * @param firstName the updated first name
     * @param lastName the updated last name
     * @param mobile the updated mobile number
     * @param email the updated email address (optional)
     * @param height the updated height in cm (optional)
     * @param weight the updated weight in kg (optional)
     * @param gender the updated gender
     * @param dateOfBirth the updated date of birth
     * @param payment the updated payment amount in LE
     * @param startDate the updated membership start date
     * @param assignedCoach the updated assigned coach ID (optional)
     * @param assignedBranch the updated assigned branch ID
     * @param isActive the updated active status
     * @param ipAddress the IP address for audit logging
     * @return true if update was successful, false otherwise
     */
    public boolean updateMember(
            int memberId,
            String firstName,
            String lastName,
            String mobile,
            String email,
            BigDecimal height,
            BigDecimal weight,
            Gender gender,
            LocalDate dateOfBirth,
            BigDecimal payment,
            LocalDate startDate,
            Integer assignedCoach,
            int assignedBranch,
            boolean isActive,
            String ipAddress) {
        
        try {
            // Normalize mobile number (remove spaces, +20, etc.)
            mobile = ValidationUtil.normalizeMobile(mobile);
            
            // Find existing member
            Optional<Member> existingMemberOpt = memberDAO.findById(memberId);
            
            if (existingMemberOpt.isEmpty()) {
                System.err.println("Member not found with ID: " + memberId);
                return false;
            }
            
            Member existingMember = existingMemberOpt.get();
            
            // Validate inputs
            ValidationUtil.ValidationResult validation = validateMemberInputs(
                firstName, lastName, mobile, email, payment, startDate
            );
            
            if (!validation.isValid()) {
                System.err.println("Member update validation failed: " + validation.getMessage());
                return false;
            }
            
            // Calculate period and end date
            String period = DateUtil.calculatePeriod(payment, MONTHLY_PRICE);
            LocalDate endDate = DateUtil.calculateEndDate(startDate, payment, MONTHLY_PRICE);
            
            // Create updated member object
            Member updatedMember = new Member(
                memberId,
                existingMember.getRandomId(), // Random ID cannot be changed
                firstName,
                lastName,
                mobile,
                email,
                height,
                weight,
                gender,
                dateOfBirth,
                payment,
                startDate,
                endDate,
                period,
                assignedBranch,
                assignedCoach,
                isActive,
                existingMember.getNotes(),
                existingMember.getCreatedAt(),
                existingMember.getUpdatedAt()
            );
            
            boolean updated = memberDAO.update(updatedMember);
            
            if (updated) {
                // Log member update
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "MEMBER_UPDATE",
                    "Updated member: " + firstName + " " + lastName + " (ID: " + existingMember.getRandomId() + ")",
                    ipAddress
                );
            }
            
            return updated;
            
        } catch (Exception e) {
            System.err.println("Error updating member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a member (soft delete - sets is_active to false).
     * 
     * @param memberId the ID of the member to delete
     * @param ipAddress the IP address for audit logging
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteMember(int memberId, String ipAddress) {
        try {
            Optional<Member> memberOpt = memberDAO.findById(memberId);
            
            if (memberOpt.isEmpty()) {
                System.err.println("Member not found with ID: " + memberId);
                return false;
            }
            
            Member member = memberOpt.get();
            
            boolean deleted = memberDAO.delete(memberId);
            
            if (deleted) {
                // Log member deletion
                User currentUser = sessionManager.getCurrentUser();
                auditLogDAO.create(
                    currentUser != null ? currentUser.getId() : null,
                    "MEMBER_DELETE",
                    "Deleted member: " + member.getFirstName() + " " + member.getLastName() + " (ID: " + member.getRandomId() + ")",
                    ipAddress
                );
            }
            
            return deleted;
            
        } catch (Exception e) {
            System.err.println("Error deleting member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Finds a member by ID.
     */
    public Optional<Member> findById(int memberId) {
        return memberDAO.findById(memberId);
    }
    
    /**
     * Finds a member by random ID.
     */
    public Optional<Member> findByRandomId(String randomId) {
        try {
            int randomIdInt = Integer.parseInt(randomId);
            return memberDAO.findByRandomId(randomIdInt);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Gets all members assigned to a specific branch.
     */
    /**
     * Gets all members in the system.
     */
    public List<Member> getAllMembers() {
        return memberDAO.findAll();
    }
    
    public List<Member> getMembersByBranch(int branchId) {
        return memberDAO.findByBranch(branchId);
    }
    
    /**
     * Gets all members assigned to a specific coach.
     */
    public List<Member> getMembersByCoach(int coachId) {
        return memberDAO.findByCoach(coachId);
    }
    
    /**
     * Searches for members by keyword and optional branch filter.
     * 
     * @param keyword the search keyword (name, mobile, random ID)
     * @param branchId the branch ID to filter by (null for all branches)
     * @return list of matching members
     */
    public List<Member> searchMembers(String keyword, Integer branchId) {
        return memberDAO.search(keyword, branchId);
    }
    
    /**
     * Gets the count of members in a specific branch.
     */
    public int getMemberCountByBranch(int branchId) {
        return memberDAO.countByBranch(branchId);
    }
    
    /**
     * Generates a unique 8-digit random ID for a new member.
     * Tries up to 10 times to find a unique ID.
     * 
     * @return the unique random ID as a string, or null if failed
     */
    private String generateUniqueRandomId() {
        int maxAttempts = 10;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate 8-digit number (10000000 to 99999999)
            int randomNumber = 10000000 + random.nextInt(90000000);
            String randomId = String.valueOf(randomNumber);
            
            // Check if ID already exists
            Optional<Member> existing = memberDAO.findByRandomId(randomNumber);
            
            if (existing.isEmpty()) {
                return randomId; // Found unique ID
            }
        }
        
        return null; // Failed to find unique ID after max attempts
    }
    
    /**
     * Validates member input fields.
     */
    private ValidationUtil.ValidationResult validateMemberInputs(
            String firstName,
            String lastName,
            String mobile,
            String email,
            BigDecimal payment,
            LocalDate startDate) {
        
        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Last name is required");
        }
        
        if (mobile == null || mobile.trim().isEmpty()) {
            return new ValidationUtil.ValidationResult(false, "Mobile is required");
        }
        
        if (payment == null || payment.compareTo(BigDecimal.ZERO) <= 0) {
            return new ValidationUtil.ValidationResult(false, "Payment must be greater than zero");
        }
        
        if (startDate == null) {
            return new ValidationUtil.ValidationResult(false, "Start date is required");
        }
        
        // Validate mobile format
        ValidationUtil.ValidationResult mobileValidation = ValidationUtil.validateMobile(mobile);
        if (!mobileValidation.isValid()) {
            return mobileValidation;
        }
        
        // Validate email format (if provided)
        if (email != null && !email.trim().isEmpty()) {
            ValidationUtil.ValidationResult emailValidation = ValidationUtil.validateEmail(email);
            if (!emailValidation.isValid()) {
                return emailValidation;
            }
        }
        
        return new ValidationUtil.ValidationResult(true, "Valid");
    }
}
