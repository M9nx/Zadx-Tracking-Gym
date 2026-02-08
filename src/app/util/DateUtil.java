package app.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DateUtil - Date calculation utilities
 * Provides period calculation for gym memberships
 */
public class DateUtil {
    
    /**
     * Calculate membership period between two dates
     * @param startDate Start date
     * @param endDate End date
     * @return Human-readable period string (e.g., "30 days", "3 months")
     */
    public static String calculatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Invalid dates";
        }
        
        if (endDate.isBefore(startDate)) {
            return "Invalid: End date before start date";
        }
        
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        long years = ChronoUnit.YEARS.between(startDate, endDate);
        
        if (years > 0) {
            return years + (years == 1 ? " year" : " years");
        } else if (months > 0) {
            return months + (months == 1 ? " month" : " months");
        } else {
            return days + (days == 1 ? " day" : " days");
        }
    }
    
    /**
     * Calculate end date based on start date and period in months
     * @param startDate Start date
     * @param months Number of months
     * @return End date
     */
    public static LocalDate calculateEndDate(LocalDate startDate, int months) {
        return startDate.plusMonths(months);
    }
    
    /**
     * Calculate end date based on start date and period in days
     * @param startDate Start date
     * @param days Number of days
     * @return End date
     */
    public static LocalDate calculateEndDateFromDays(LocalDate startDate, int days) {
        return startDate.plusDays(days);
    }
    
    /**
     * Check if membership is expired
     * @param endDate End date of membership
     * @return true if expired, false otherwise
     */
    public static boolean isExpired(LocalDate endDate) {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }
    
    /**
     * Check if membership is expiring soon (within specified days)
     * @param endDate End date of membership
     * @param daysThreshold Number of days threshold
     * @return true if expiring within threshold, false otherwise
     */
    public static boolean isExpiringSoon(LocalDate endDate, int daysThreshold) {
        if (endDate == null || endDate.isBefore(LocalDate.now())) {
            return false;
        }
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return daysRemaining <= daysThreshold;
    }
    
    /**
     * Get days remaining until end date
     * @param endDate End date
     * @return Number of days remaining (negative if expired)
     */
    public static long getDaysRemaining(LocalDate endDate) {
        if (endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
    
    /**
     * Calculate period from payment amount (150 LE per month).
     * @param payment Payment amount
     * @param monthlyPrice Monthly membership price
     * @return Human-readable period string (e.g., "1 month", "3 months")
     */
    public static String calculatePeriod(java.math.BigDecimal payment, java.math.BigDecimal monthlyPrice) {
        if (payment == null || monthlyPrice == null || monthlyPrice.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "Invalid payment";
        }
        
        // Calculate months
        java.math.BigDecimal months = payment.divide(monthlyPrice, 2, java.math.RoundingMode.HALF_UP);
        
        // If less than 1 month, calculate days
        if (months.compareTo(java.math.BigDecimal.ONE) < 0) {
            // Approximate days per month
            int days = months.multiply(new java.math.BigDecimal("30")).intValue();
            return days + (days == 1 ? " day" : " days");
        }
        
        int fullMonths = months.intValue();
        return fullMonths + (fullMonths == 1 ? " month" : " months");
    }
    
    /**
     * Calculate end date from start date and payment amount (150 LE per month).
     * @param startDate Start date
     * @param payment Payment amount
     * @param monthlyPrice Monthly membership price
     * @return Calculated end date
     */
    public static LocalDate calculateEndDate(LocalDate startDate, java.math.BigDecimal payment, java.math.BigDecimal monthlyPrice) {
        if (startDate == null || payment == null || monthlyPrice == null || monthlyPrice.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return startDate;
        }
        
        // Calculate months
        java.math.BigDecimal months = payment.divide(monthlyPrice, 2, java.math.RoundingMode.HALF_UP);
        
        // If less than 1 month, calculate in days (30 days per month approximation)
        if (months.compareTo(java.math.BigDecimal.ONE) < 0) {
            int days = months.multiply(new java.math.BigDecimal("30")).intValue();
            return startDate.plusDays(days);
        }
        
        int fullMonths = months.intValue();
        return startDate.plusMonths(fullMonths);
    }
    
    /**
     * Format period information with dates
     * @param startDate Start date
     * @param endDate End date
     * @return Formatted string (e.g., "3 months (2025-01-01 to 2025-04-01)")
     */
    public static String formatPeriodWithDates(LocalDate startDate, LocalDate endDate) {
        String period = calculatePeriod(startDate, endDate);
        return String.format("%s (%s to %s)", period, startDate, endDate);
    }
}
