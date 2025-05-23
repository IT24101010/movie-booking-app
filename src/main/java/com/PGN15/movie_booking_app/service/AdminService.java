package com.hiruna.movieticketbooking.service;

import com.hiruna.movieticketbooking.model.Admin;
import com.hiruna.movieticketbooking.repository.FileHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Admin management.
 * Handles business logic related to administrators.
 */
@Service
public class AdminService {

    /**
     * Registers a new admin.
     * Checks if the admin username already exists.
     * @param admin The admin object to register.
     * @return The registered admin, or null if username already exists or registration fails.
     */
    public Admin registerAdmin(Admin admin) {
        List<Admin> admins = getAllAdminsInternal();
        boolean usernameExists = admins.stream()
                .anyMatch(a -> a.getUsername().equalsIgnoreCase(admin.getUsername()));
        if (usernameExists) {
            System.err.println("Admin registration failed: Username '" + admin.getUsername() + "' already exists.");
            return null;
        }
        FileHandler.appendToFile(FileHandler.ADMINS_FILE, admin.toCsvString());
        return admin;
    }

    /**
     * Logs in an admin by verifying their username and password.
     * @param username The username provided for login.
     * @param password The password provided for login.
     * @return An Optional containing the Admin if credentials are valid, otherwise an empty Optional.
     */
    public Optional<Admin> loginAdmin(String username, String password) {
        return getAllAdminsInternal().stream()
                .filter(admin -> admin.getUsername().equalsIgnoreCase(username) && admin.getPassword().equals(password))
                .findFirst();
    }

    /**
     * Retrieves an admin by their unique user ID.
     * @param adminId The ID of the admin to find.
     * @return An Optional containing the Admin if found, otherwise an empty Optional.
     */
    public Optional<Admin> getAdminById(String adminId) {
        return getAllAdminsInternal().stream()
                .filter(admin -> admin.getUserId().equals(adminId)) // Assuming Admin inherits userId
                .findFirst();
    }

    /**
     * Retrieves an admin by their username.
     * @param username The username of the admin to find.
     * @return An Optional containing the Admin if found, otherwise an empty Optional.
     */
    public Optional<Admin> getAdminByUsername(String username) {
        return getAllAdminsInternal().stream()
                .filter(admin -> admin.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }


    /**
     * Updates an existing admin's details.
     * The admin is identified by their userId.
     * @param updatedAdmin The admin object containing the new details.
     * @return The updated admin object if successful, or null if not found or update failed.
     */
    public Admin updateAdmin(Admin updatedAdmin) {
        if (getAdminById(updatedAdmin.getUserId()).isEmpty()) {
            System.err.println("Admin update failed: Admin with ID '" + updatedAdmin.getUserId() + "' not found.");
            return null;
        }
        boolean success = FileHandler.updateLineById(FileHandler.ADMINS_FILE, updatedAdmin.getUserId(), updatedAdmin.toCsvString());
        return success ? updatedAdmin : null;
    }

    /**
     * Deletes an admin account.
     * @param adminId The ID of the admin to delete.
     * @return true if the admin was successfully deleted, false otherwise.
     */
    public boolean deleteAdmin(String adminId) {
        if (getAdminById(adminId).isEmpty()) {
            System.err.println("Admin deletion failed: Admin with ID '" + adminId + "' not found.");
            return false;
        }
        return FileHandler.deleteLineById(FileHandler.ADMINS_FILE, adminId);
    }

    /**
     * Retrieves all registered admins.
     * @return A list of all Admin objects.
     */
    public List<Admin> getAllAdmins() {
        return getAllAdminsInternal();
    }

    /**
     * Internal helper method to read and parse all admins from the file.
     * @return A list of Admin objects.
     */
    private List<Admin> getAllAdminsInternal() {
        return FileHandler.readFile(FileHandler.ADMINS_FILE).stream()
                .map(Admin::fromAdminCsvString) // Use the static factory method in Admin
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
