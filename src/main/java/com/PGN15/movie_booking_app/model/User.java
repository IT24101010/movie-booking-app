package com.hiruna.movieticketbooking.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a registered user in the system.
 * Inherits from BaseUser, demonstrating inheritance.
 * Polymorphism can be applied if User objects are treated as BaseUser
 * in collections or method parameters, and specific User behavior is invoked.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) // Important for Lombok when extending
@ToString(callSuper = true)         // Important for Lombok when extending
public class User extends BaseUser {
    // User can have additional specific fields if needed in the future.
    // For example, membership status, preferences, etc.
    // For now, it's structurally the same as BaseUser but provides a distinct type.

    /**
     * Constructor for creating a User with all fields including ID.
     * @param userId User's unique identifier.
     * @param username User's chosen username.
     * @param password User's chosen password.
     * @param email User's email address.
     * @param contactNumber User's contact phone number.
     */
    public User(String userId, String username, String password, String email, String contactNumber) {
        super(userId, username, password, email, contactNumber);
    }

    /**
     * Constructor for creating a new User (ID will be auto-generated).
     * @param username User's chosen username.
     * @param password User's chosen password.
     * @param email User's email address.
     * @param contactNumber User's contact phone number.
     */
    public User(String username, String password, String email, String contactNumber) {
        super(username, password, email, contactNumber);
    }

    /**
     * Creates a User object from a CSV string.
     * This static factory method is specific to the User class.
     * @param csvString The CSV string representing a User.
     * @return A User object, or null if the CSV is malformed.
     */
    public static User fromUserCsvString(String csvString) {
        BaseUser baseUser = BaseUser.fromCsvString(csvString); // Utilize base class parsing
        if (baseUser == null) {
            return null;
        }
        // If User had additional fields in CSV, parse them here.
        return new User(baseUser.getUserId(), baseUser.getUsername(), baseUser.getPassword(), baseUser.getEmail(), baseUser.getContactNumber());
    }

    // toCsvString() is inherited from BaseUser and should work fine if User has no additional fields to serialize.
    // If User had additional fields, you would override toCsvString() here:
    // @Override
    // public String toCsvString() {
    //     return super.toCsvString() + "," + additionalField1 + "," + additionalField2;
    // }
}

