package com.hiruna.movieticketbooking.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents an Administrator in the system.
 * Inherits from BaseUser, demonstrating inheritance.
 * May have additional privileges or attributes specific to administrators.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Admin extends BaseUser {
    private String adminRole; // Example: "SUPER_ADMIN", "CONTENT_ADMIN", "USER_MANAGER"

    /**
     * Constructor for creating an Admin with all fields including ID.
     * @param userId Admin's unique identifier.
     * @param username Admin's username.
     * @param password Admin's password.
     * @param email Admin's email address.
     * @param contactNumber Admin's contact number.
     * @param adminRole The role or privilege level of the admin.
     */
    public Admin(String userId, String username, String password, String email, String contactNumber, String adminRole) {
        super(userId, username, password, email, contactNumber);
        this.adminRole = adminRole;
    }

    /**
     * Constructor for creating a new Admin (ID will be auto-generated).
     * @param username Admin's username.
     * @param password Admin's password.
     * @param email Admin's email address.
     * @param contactNumber Admin's contact number.
     * @param adminRole The role or privilege level of the admin.
     */
    public Admin(String username, String password, String email, String contactNumber, String adminRole) {
        super(username, password, email, contactNumber); // Calls BaseUser constructor to generate userId
        this.adminRole = adminRole;
    }

    /**
     * Converts Admin object to a CSV string for file storage.
     * Overrides BaseUser's method to include admin-specific fields.
     * Format: userId,username,password,email,contactNumber,adminRole
     * @return CSV string representation of the admin.
     */
    @Override
    public String toCsvString() {
        return String.join(",", super.toCsvString(), escapeCsv(adminRole));
    }

    /**
     * Creates an Admin object from a CSV string.
     * This static factory method is specific to the Admin class.
     * @param csvString The CSV string representing an Admin.
     * @return An Admin object, or null if the CSV is malformed.
     */
    public static Admin fromAdminCsvString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvString.split(",", 6); // Expect 6 parts for Admin
        if (parts.length < 6) {
            System.err.println("Malformed CSV string for Admin: " + csvString + ". Expected 6 parts, got " + parts.length);
            return null;
        }
        // Parts 0-4 are for BaseUser, part 5 is for adminRole
        return new Admin(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
}
