package com.hiruna.movieticketbooking.controller;


import com.hiruna.movieticketbooking.model.Admin;
import com.hiruna.movieticketbooking.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty() ||
                admin.getPassword() == null || admin.getPassword().trim().isEmpty() ||
                admin.getEmail() == null || admin.getEmail().trim().isEmpty() ||
                admin.getAdminRole() == null || admin.getAdminRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username, password, email, and adminRole are required.");
        }
        Admin registeredAdmin = adminService.registerAdmin(new Admin(admin.getUsername(), admin.getPassword(), admin.getEmail(), admin.getContactNumber(), admin.getAdminRole()));
        if (registeredAdmin != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredAdmin);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Admin username already exists or registration failed.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody UserController.LoginRequest loginRequest) {
        Optional<Admin> adminOptional = adminService.loginAdmin(loginRequest.getUsername(), loginRequest.getPassword());
        if (adminOptional.isPresent()) {
            return ResponseEntity.ok(adminOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid admin username or password.");
        }
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String adminId) {
        return adminService.getAdminById(adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @PutMapping("/{adminId}")
    public ResponseEntity<?> updateAdmin(@PathVariable String adminId, @RequestBody Admin adminDetails) {
        adminDetails.setUserId(adminId);
        Admin updatedAdmin = adminService.updateAdmin(adminDetails);
        if (updatedAdmin != null) {
            return ResponseEntity.ok(updatedAdmin);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found or update failed.");
        }
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String adminId) {
        if (adminService.deleteAdmin(adminId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
