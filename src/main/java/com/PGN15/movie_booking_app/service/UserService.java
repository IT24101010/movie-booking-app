package com.PGN15.movieticketbooking.service;

import com.PGN15.movieticketbooking.model.User;
import com.PGN15.movieticketbooking.repository.FileHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for User management.
 * Handles business logic related to users, interacting with FileHandler for data persistence.
 */
@Service
public class UserService {

    /**
     * Registers a new user.
     * Checks if the username already exists before adding.
     * @param user The user object to register (ID will be generated if not present).
     * @return The registered user with a generated ID, or null if username already exists or registration fails.
     */
    public User registerUser(User user) {
        List<User> users = getAllUsersInternal();
        boolean usernameExists = users.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(user.getUsername()));
        if (usernameExists) {
            System.err.println("Registration failed: Username '" + user.getUsername() + "' already exists.");
            return null;
        }
        // If user object was created without an ID (e.g. from controller),
        // its constructor should have generated one.
        // In a real app, hash the password here: user.setPassword(passwordEncoder.encode(user.getPassword()));
        FileHandler.appendToFile(FileHandler.USERS_FILE, user.toCsvString());
        return user;
    }

    /**
     * Logs in a user by verifying their username and password.
     * @param username The username provided for login.
     * @param password The password provided for login.
     * @return An Optional containing the User if credentials are valid, otherwise an empty Optional.
     */
    public Optional<User> loginUser(String username, String password) {
        return getAllUsersInternal().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password))
                .findFirst();
    }

    /**
     * Retrieves a user by their unique user ID.
     * @param userId The ID of the user to find.
     * @return An Optional containing the User if found, otherwise an empty Optional.
     */
    public Optional<User> getUserById(String userId) {
        return getAllUsersInternal().stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * Retrieves a user by their username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise an empty Optional.
     */
    public Optional<User> getUserByUsername(String username) {
        return getAllUsersInternal().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Updates an existing user's profile details.
     * The user is identified by their userId.
     * @param updatedUser The user object containing the new details. The userId field must match an existing user.
     * @return The updated user object if successful, or null if the user was not found or the update failed.
     */
    public User updateUser(User updatedUser) {
        // Ensure the user exists before attempting to update
        if (getUserById(updatedUser.getUserId()).isEmpty()) {
            System.err.println("Update failed: User with ID '" + updatedUser.getUserId() + "' not found.");
            return null;
        }
        boolean success = FileHandler.updateLineById(FileHandler.USERS_FILE, updatedUser.getUserId(), updatedUser.toCsvString());
        return success ? updatedUser : null;
    }

    /**
     * Deletes a user account from the system.
     * @param userId The ID of the user to delete.
     * @return true if the user was successfully deleted, false otherwise (e.g., user not found).
     */
    public boolean deleteUser(String userId) {
        // Before deleting, check if user exists to provide accurate return value
        if (getUserById(userId).isEmpty()) {
            System.err.println("Deletion failed: User with ID '" + userId + "' not found.");
            return false;
        }
        // Consider implications: what happens to bookings made by this user?
        // For this project, we might just delete the user record.
        return FileHandler.deleteLineById(FileHandler.USERS_FILE, userId);
    }

    /**
     * Retrieves all registered users from the data file.
     * This method is public and can be used by controllers.
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        return getAllUsersInternal();
    }

    /**
     * Internal helper method to read and parse all users from the file.
     * @return A list of User objects.
     */
    private List<User> getAllUsersInternal() {
        return FileHandler.readFile(FileHandler.USERS_FILE).stream()
                .map(User::fromUserCsvString) // Use the static factory method in User
                .filter(Objects::nonNull)     // Filter out any nulls from parsing errors
                .collect(Collectors.toList());
    }
}
