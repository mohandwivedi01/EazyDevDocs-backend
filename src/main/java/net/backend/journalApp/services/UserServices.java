package net.backend.journalApp.services;

import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.model.UserModel;
import net.backend.journalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Service annotation to indicate this class is a service component for Spring
@Service
@Slf4j // Slf4j annotation provides a logger instance, no need for LoggerFactory
public class UserServices {

    // Autowired to inject the UserRepository for database access
    @Autowired
    private UserRepository userRepository;

    // Password encoder for hashing the user password
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Method to save a user to the repository
    public void saveUser(UserModel user){
        log.info("Saving user: {}", user.getUserName());
        userRepository.save(user);                              // Save user in the database
        log.info("User saved successfully: {}", user.getUserName());
    }

    // Method to save a new user with encrypted password
    public boolean saveNewUser(UserModel user){
        try{
            log.info("Signing up new user: {}", user.getUserName());

            // Encode the user's password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(user.getRoles());  // Assign roles to the user
            userRepository.save(user);  // Save the user
            log.info("New user signed up successfully: {}", user.getUserName());
            return true;
        }catch (Exception e){
            // Log the error if any exception occurs during user signup
            log.error("Something went wrong during signup for user: {}", user.getUserName(), e);
            return false;  // Return false if there was an error
        }
    }

    // Method to get all users from the repository
    public List<UserModel> getAll(){
        log.info("Fetching all users...");
        List<UserModel> users = userRepository.findAll();
        log.info("Total users retrieved: {}", users.size());
        return users;
    }

    // Method to find a user by their ID
    public Optional<UserModel> getUserById(ObjectId id){
        log.info("Fetching user with ID: {}", id);
        Optional<UserModel> user = userRepository.findById(id);
        if (user.isPresent()) {
            log.info("User found with ID: {}", id);
        } else {
            log.warn("User not found with ID: {}", id);
        }
        return user;
    }

    // Method to delete a user by their ID
    public Optional<UserModel> deleteUserById(ObjectId id){
        log.info("Attempting to delete user with ID: {}", id);
        Optional<UserModel> user = userRepository.findById(id);  // Get user by ID

        if (user.isPresent()) {
            userRepository.deleteById(id);
            log.info("User deleted successfully with ID: {}", id);
        } else {
            log.warn("User not found with ID: {}, unable to delete", id);
        }
        return user;
    }

    /**
     * Find user by username
     */
    public UserModel findByUserName(String userName) {
        log.info("Searching for user by username: {}", userName);
        UserModel user = userRepository.findByUserName(userName);

        if (user != null) {
            log.info("User found: {}", userName);
        } else {
            log.warn("User not found: {}", userName);
        }
        return user;
    }

    /**
     * Update user's username and password
     */
    public boolean updateUserDetails(ObjectId userId, String newUserName, String newPassword) {
        log.info("Updating user details for user ID: {}", userId);

        Optional<UserModel> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserModel user = userOptional.get();

            // Update username if provided
            if (newUserName != null && !newUserName.trim().isEmpty()) {
                log.info("Updating username for user ID: {} -> New Username: {}", userId, newUserName);
                user.setUserName(newUserName);
            }

            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                log.info("Updating password for user ID: {}", userId);
                user.setPassword(passwordEncoder.encode(newPassword)); // Encrypt password before saving
            }

            userRepository.save(user);
            log.info("User details updated successfully for user ID: {}", userId);
            return true;
        } else {
            log.warn("User not found with ID: {}, update failed", userId);
            return false;
        }
    }
}


