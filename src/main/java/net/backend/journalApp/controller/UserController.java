package net.backend.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.model.UserModel;
import net.backend.journalApp.services.UserServices;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController // This class serves as a REST controller.
@RequestMapping("/api/v1/user") // All methods in this controller are mapped to "/user".
public class UserController {

    @Autowired
    private UserServices userServices; // Service for user-related operations.


    /**
     * Endpoint to retrieve a user's details by their ID.
     *
     * @param id The ID of the user.
     * @return A ResponseEntity containing the user information or a 404 if the user is not found.
     */
    @GetMapping("/id{id}") // Maps the endpoint to "/user/id{id}".
    public Optional<UserModel> getUserById(@PathVariable ObjectId id) {
        return userServices.getUserById(id); // Retrieves a user by their ID.
    }

    /**
     * Endpoint to update a user's details.
     * Only updates the username and password.
     *
     * @param user The user model containing the updated details.
     * @return HTTP 200 (OK) if the update is successful, HTTP 204 (NO_CONTENT) if the user doesn't exist.
     */
    @PutMapping // Maps to "/user" for updating the user details.
    public ResponseEntity<?> updateUser(@RequestBody UserModel user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName(); // Retrieves the username from the authentication context.

        log.info("User '{}' attempting to update their profile.", userName);

        // Finds the existing user by username.
        UserModel isUser = userServices.findByUserName(userName);
        if (isUser != null) {
            // Update the user's details with the new ones.
            isUser.setUserName(user.getUserName());
            isUser.setPassword(user.getPassword());
            userServices.saveNewUser(isUser); // Save the updated user.
            log.info("User '{}' updated successfully.", userName);
            return new ResponseEntity<>("User updated successfully.", HttpStatus.OK); // Return HTTP 200 if the update is successful.
        }
        log.warn("User '{}' not found in the database.", userName);
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND); // Return HTTP 204 if the user is not found.
    }


}

