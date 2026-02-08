package app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Member Model - Represents a gym member
 * Immutable DTO for member data
 */
public class Member {
    private final int memberId;
    private final int randomId;
    private final String firstName;
    private final String lastName;
    private final String mobile;
    private final String email;
    private final BigDecimal height; // in cm
    private final BigDecimal weight; // in kg
    private final Gender gender;
    private final LocalDate dateOfBirth;
    private final BigDecimal payment;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String period;
    private final int assignedBranch;
    private final Integer assignedCoach; // Nullable
    private final boolean isActive;
    private final String notes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Constructor for new members (without ID)
    public Member(int randomId, String firstName, String lastName, String mobile, String email,
                  BigDecimal height, BigDecimal weight, Gender gender, LocalDate dateOfBirth,
                  BigDecimal payment, LocalDate startDate, LocalDate endDate, String period,
                  int assignedBranch, Integer assignedCoach, String notes) {
        this(0, randomId, firstName, lastName, mobile, email, height, weight, gender,
             dateOfBirth, payment, startDate, endDate, period, assignedBranch,
             assignedCoach, true, notes, null, null);
    }

    // Full constructor (from database)
    public Member(int memberId, int randomId, String firstName, String lastName,
                  String mobile, String email, BigDecimal height, BigDecimal weight,
                  Gender gender, LocalDate dateOfBirth, BigDecimal payment,
                  LocalDate startDate, LocalDate endDate, String period,
                  int assignedBranch, Integer assignedCoach, boolean isActive,
                  String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.memberId = memberId;
        this.randomId = randomId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.payment = payment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
        this.assignedBranch = assignedBranch;
        this.assignedCoach = assignedCoach;
        this.isActive = isActive;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getMemberId() { return memberId; }
    public int getId() { return memberId; } // Alias for compatibility
    public int getRandomId() { return randomId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public BigDecimal getHeight() { return height; }
    public BigDecimal getWeight() { return weight; }
    public Gender getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public BigDecimal getPayment() { return payment; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getPeriod() { return period; }
    public int getAssignedBranch() { return assignedBranch; }
    public Integer getAssignedCoach() { return assignedCoach; }
    public boolean isActive() { return isActive; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Helper methods
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public long getDaysRemaining() {
        if (endDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    public MembershipStatus getMembershipStatus() {
        if (!isActive) return MembershipStatus.INACTIVE;
        if (isExpired()) return MembershipStatus.EXPIRED;
        if (getDaysRemaining() <= 7) return MembershipStatus.EXPIRING_SOON;
        return MembershipStatus.ACTIVE;
    }

    // Create new member with updated fields
    public Member withId(int newId) {
        return new Member(newId, randomId, firstName, lastName, mobile, email,
                         height, weight, gender, dateOfBirth, payment, startDate,
                         endDate, period, assignedBranch, assignedCoach, isActive,
                         notes, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getFullName() + " (ID: " + randomId + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Member member = (Member) obj;
        return memberId == member.memberId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(memberId);
    }

    public enum Gender {
        MALE("male", "Male"),
        FEMALE("female", "Female"),
        OTHER("other", "Other");

        private final String dbValue;
        private final String displayName;

        Gender(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static Gender fromString(String text) {
            for (Gender g : Gender.values()) {
                if (g.dbValue.equalsIgnoreCase(text)) {
                    return g;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }

        @Override
        public String toString() { return displayName; }
    }

    public enum MembershipStatus {
        ACTIVE, EXPIRING_SOON, EXPIRED, INACTIVE
    }
}
