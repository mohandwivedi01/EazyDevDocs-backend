package net.backend.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.model.UserModel;
import net.backend.journalApp.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The `AdminController` class handles administrative endpoints.
 * It provides functionality for administrators, such as fetching all user details.
 */
@Slf4j
@RestController // Indicates that this class is a REST controller.
@RequestMapping("/api/v1/admin") // Maps all endpoints in this class under the "/admin" base URL.
public class AdminController {

    @Autowired
    private UserServices userServices; // Service layer to handle user-related operations.

    /**
     * Retrieves all registered users in the system.
     *
     * @return A `ResponseEntity` containing the list of all users and an HTTP status.
     * - Returns HTTP 200 (OK) with the user list if users are found.
     * - Returns HTTP 404 (NOT_FOUND) if no users are found.
     */
    @GetMapping("/all-users") // Maps this method to the "/admin/all-users" GET endpoint.
    public ResponseEntity<?> getAllUsers() {
        log.info("Fetching all registered users.");

        try {
            // Fetch all users from the database using the service layer.
            List<UserModel> allUsers = userServices.getAll();

            // Check if the list is not null and contains data.
            if (allUsers != null && !allUsers.isEmpty()) {
                log.info("Successfully fetched {} users.", allUsers.size());
                return new ResponseEntity<>(allUsers, HttpStatus.OK); // Return user list with HTTP 200.
            }

            log.warn("No users found in the system.");
            return new ResponseEntity<>("No users found.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while fetching users.");
        }
    }
}

