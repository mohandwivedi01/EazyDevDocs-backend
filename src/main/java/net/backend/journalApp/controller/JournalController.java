package net.backend.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.model.JournalModel;
import net.backend.journalApp.model.UserModel;
import net.backend.journalApp.services.CloudinaryService;
import net.backend.journalApp.services.JournalServices;
import net.backend.journalApp.services.UserServices;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/journal")
public class JournalController {

    @Autowired
    private JournalServices journalServices;

    @Autowired
    private UserServices userServices;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/get-all-user-journals")
    public ResponseEntity<List<JournalModel>> getAllUserJournals() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            log.info("Fetching journal entries for user: {}", userName);

            UserModel user = userServices.findByUserName(userName);
            List<JournalModel> journals = user.getJournalEntries();

            if (journals == null || journals.isEmpty()) {
                log.warn("No journal entries found for user: {}", userName);
                return ResponseEntity.noContent().build();
            }

            log.info("Successfully fetched {} journal entries for user: {}", journals.size(), userName);
            return ResponseEntity.ok(journals);
        } catch (Exception e) {
            log.error("Error fetching all user journals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint to fetch all journal entries
     */
    @GetMapping("/get-all-journals")
    public ResponseEntity<List<JournalModel>> getAllJournals() {
        log.info("Received request to fetch all journals.");

        try {

            List<JournalModel> journals = journalServices.findAllJournals();

            if (journals.isEmpty()) {
                log.info("No journals found. Returning 204 No Content.");
                return ResponseEntity.noContent().build();
            }

            log.info("Returning {} journal entries.", journals.size());
            return ResponseEntity.ok(journals);

        } catch (Exception e) {
            log.error("Error fetching all journals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/id/{journalId}")
    public ResponseEntity<?> getJournalById(@PathVariable ObjectId journalId) {
        try {
            log.info("Fetching journal entry with ID: {}", journalId);
            ResponseEntity<?> response = journalServices.findJournalById(journalId);
            if (response == null) {
                log.warn("Journal entry not found with ID: {}", journalId);
                return ResponseEntity.status(404).body("Journal entry not found");
            }
            log.info("Successfully retrieved journal entry with ID: {}", journalId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching journal by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/post")
    public ResponseEntity<String> createEntry(
            @RequestParam("image") MultipartFile file,
            @ModelAttribute JournalModel journalEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            log.info("Creating a new journal entry for user: {}", userName);
            String imageUrl = "";
            if (!file.isEmpty()) {
                log.info("Uploading image for new journal entry...");
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                imageUrl = (String) uploadResult.get("url");
                if (imageUrl == null) {
                    log.error("Image upload failed for user: {}", userName);
                    return ResponseEntity.badRequest().body("Image upload failed");
                }
                log.info("Image uploaded successfully: {}", imageUrl);
            }

            ResponseEntity<?> journal = journalServices.saveJournalEntries(journalEntry, userName, (imageUrl));
            log.info("Journal entry created successfully for user: {}", userName);
            return ResponseEntity.status(HttpStatus.CREATED).body(journal + "Journal entry created successfully");
        } catch (Exception e) {
            log.error("Error creating a journal entry for user: {}", SecurityContextHolder.getContext().getAuthentication().getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @DeleteMapping("delete/id/{id}")
    public ResponseEntity<String> deleteJournalById(@PathVariable ObjectId id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            log.info("Attempting to delete journal entry with ID: {} for user: {}", id, userName);

            boolean deleted = journalServices.deleteJournalById(id, userName).hasBody();
            if (deleted) {
                log.info("Successfully deleted journal entry with ID: {} for user: {}", id, userName);
                return ResponseEntity.ok("Journal entry deleted successfully");
            } else {
                log.warn("Journal entry not found or deletion failed for ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting journal entry with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PutMapping("update/id/{journalId}")
    public ResponseEntity<?> updateJournalEntry(
            @PathVariable ObjectId journalId,
            @RequestParam(value = "image", required = false) MultipartFile file,
            @ModelAttribute JournalModel updatedJournal
    ) {
        try {
            //Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Updating journal entry with ID: {} for user: {}", journalId, username);

            //fetch the user
            UserModel user = userServices.findByUserName(username);
            if (user == null) {
                log.warn("Unauthorized access attempt by user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not found or not authorized.");
            }

            //Handle image upload if a new file is provided
            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                log.info("Uploading new image for journal entry update...");
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                imageUrl = (String) uploadResult.get("url");

                if (imageUrl == null) {
                    log.error("Image upload failed during journal update for user: {}", username);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image uplload faild.");
                }
            }
            log.info("Image uploaded successfully for journal update: {}", imageUrl);
            //call the server method to update the journal entry
            ResponseEntity<?> response = journalServices.updateJournalEntry(journalId, updatedJournal, username, imageUrl);
            log.info("Journal entry with ID: {} updated successfully for user: {}", journalId, username);
            return response;

        } catch (Exception e) {
            // Log the error
            log.error("Error updating journal entry with ID: {}", journalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}
