package net.backend.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.model.UserModel;
import net.backend.journalApp.services.UserDetailsServiceImpl;
import net.backend.journalApp.services.UserServices;
import net.backend.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController // Indicates that this class is a REST controller.
@RequestMapping("/api/v1/public") // Maps all endpoints in this controller under the "/public" path.
@Slf4j // Enables logging functionality using Lombok.
public class PublicController {

    @Autowired
    private UserServices userServices; // Service for user-related operations.

    @Autowired
    private AuthenticationManager authenticationManager; // Manages user authentication.

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Custom implementation of Spring Security's UserDetailsService.

    @Autowired
    private JwtUtil jwtUtil; // Utility class for generating and validating JWTs.

    /**
     * Health check endpoint to verify if the application is running.
     *
     * @return A simple string response indicating the application is running.
     */
    @GetMapping
    public String healthCheck() {
        return "I'm running...";
    }

    /**
     * Endpoint to create a new user.
     *
     * @param user The user model containing user details (e.g., username, password).
     * @return HTTP 200 (OK) if the user is created successfully,
     *         or HTTP 404 (NOT_FOUND) if user creation fails.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserModel user) {
        log.info("Attempting to create a new user with username: {}", user.getUserName());
        try {
            boolean isUserSaved = userServices.saveNewUser(user);
            if (isUserSaved) {
                log.info("User '{}' created successfully.", user.getUserName());
                return new ResponseEntity<>("User created successfully.", HttpStatus.OK);
            }
            log.warn("User creation failed for username: {}", user.getUserName());
            return new ResponseEntity<>("User creation failed.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while creating user '{}': {}", user.getUserName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the user.");
        }
    }

    /**
     * Endpoint for user login and JWT token generation.
     *
     * @param user The user model containing username and password for authentication.
     * @return A JWT token in the response body with HTTP 200 (OK) if authentication is successful,
     *         or throws an exception if authentication fails.
     * @throws Exception If an error occurs during authentication.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserModel user) throws Exception {
            log.info("Attempting login for user: {}", user.getUserName());

       try {
            // Authenticate the user using the provided username and password.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword())
            );

            // Load user details after successful authentication.
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());

            // Generate a JWT token for the authenticated user.
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            log.info("Login successful for user: {}. JWT token generated.", user.getUserName());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Authentication failed for user '{}': {}", user.getUserName(), e.getMessage(), e);
            throw new Exception("Authentication failed.", e);
        }
    }
}



