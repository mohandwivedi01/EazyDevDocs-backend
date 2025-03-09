package net.backend.journalApp.services;

import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.model.JournalModel;
import net.backend.journalApp.model.UserModel;
import net.backend.journalApp.repository.JournalRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JournalServices {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserServices userServices;

    public JournalModel saveJournals(JournalModel journalEntity){
        log.info("Saving journal entry: {}", journalEntity);
        return journalRepository.save(journalEntity);
    }
    /**
     * Save a journal entry and associate it with a user
     */
    @Transactional
    public ResponseEntity<?> saveJournalEntries(JournalModel journalEntry, String userName, String imageUrl) {
        log.info("Saving journal entry for user: {}", userName);

        try {
            if (journalEntry.getTitle().trim().isEmpty()) {
                log.warn("Journal title is empty for user: {}", userName);
                return new ResponseEntity<>("Title field can't be empty", HttpStatus.BAD_REQUEST);
            }
            // Fetch user details
            UserModel user = userServices.findByUserName(userName);
            if (user == null) {
                log.warn("User not found: {}", userName);
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            // Ensure the journal list is initialized
            if (user.getJournalEntries() == null) {
                user.setJournalEntries(new ArrayList<>());
            }

            // Set image URL before saving
            journalEntry.setImage_url(imageUrl);
            journalEntry.setJournal_category(journalEntry.getJournal_category());
//            journalEntry.setLikes(0);
            journalEntry.setCreatedAt(LocalDateTime.now());
            journalEntry.setUpdatedAt(LocalDateTime.now());

            // Save the journal entry
            JournalModel savedJournal = journalRepository.save(journalEntry);
            log.info("Journal entry saved successfully: {}", savedJournal.getId());

            // Add the journal entry to the user's list
            user.getJournalEntries().add(savedJournal);
            userServices.saveUser(user);

            return ResponseEntity.ok(savedJournal);
        } catch (Exception e) {
            log.error("Error saving journal entry for user {}: {}", userName, e.getMessage(), e);
            return new ResponseEntity<>("An error occurred while saving the entry.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Like service
     */
//    public ResponseEntity<?> blogLike(boolean likeStatus){
//        try {
//            if(likeStatus){
//
//            }
//        }catch (Exception e){
//            return new ResponseEntity<>("An error occurred while toggling the like.", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    /**
     * Update a journal entry by ID
     */
    @Transactional
    public ResponseEntity<?> updateJournalEntry(ObjectId journalId, JournalModel updatedJournal, String userName, String imageUrl) {
        log.info("Updating journal entry: {} for user: {}", journalId, userName);

        try {

            //fetch the existing journl entry
            JournalModel existingJournal = journalRepository.findById(journalId).orElseThrow(() -> {
                log.warn("Journal entry not found: {}", journalId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");

            });

            //validate if the user ouns the journal entry
            UserModel user = userServices.findByUserName(userName);

            if (user == null || !user.getJournalEntries().contains(existingJournal)) {
                log.warn("Unauthorized update attempt by user: {}", userName);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your are not authorized to update this journal");
            }

            //update field only if new values are provided
            if (updatedJournal.getContent() != null) {
                existingJournal.setTitle(updatedJournal.getTitle());
            }
            if (updatedJournal.getUpdatedAt() != null) {
                existingJournal.setContent(updatedJournal.getContent());
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                existingJournal.setImage_url(imageUrl);
            }
            if (updatedJournal.getJournal_category() != null) {
                existingJournal.setJournal_category((updatedJournal.getJournal_category()));
            }

            //set the last updated timestamp
            existingJournal.setUpdatedAt(LocalDateTime.now());

            //save the updated journal entry
            journalRepository.save(existingJournal);

            log.info("Journal entry updated successfully: {}", existingJournal.getId());
            return ResponseEntity.ok(existingJournal);
        }catch (ResponseStatusException e) {
            log.error("Error updating journal entry: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getStatus()).body(e.getReason());
        }catch (Exception e){
            log.error("Unexpected error while updating journal entry {}: {}", journalId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the entry.");
        }
    }

    /**
     * Fetch all journal entries from the database.
     * @return List of all journal entries.
     */
    public List<JournalModel> findAllJournals() {
        log.info("Fetching all journal entries...");
        List<JournalModel> journals = journalRepository.findAll();

        if (journals.isEmpty()) {
            log.warn("No journal entries found.");
        } else {
            log.info("Retrieved {} journal entries.", journals.size());
        }

        return journals;
    }

    /**
     * Get a journal entry by ID
     */
    public ResponseEntity<?> findJournalById(ObjectId id) {
        log.info("Fetching journal entry by ID: {}", id);
        Optional<JournalModel> journal = journalRepository.findById(id);

        if (journal.isPresent()) {
            log.info("Journal entry found: {}", id);
            return new ResponseEntity<>(journal.get(), HttpStatus.OK);
        } else {
            log.warn("Journal entry not found: {}", id);
            return new ResponseEntity<>("Journal entry not found", HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Delete a journal entry by ID and dissociate it from the u ser
     */
    @Transactional
    public ResponseEntity<?> deleteJournalById(ObjectId journalId, String userName) {
        log.info("Deleting journal entry: {} for user: {}", journalId, userName);
        try {
            //fetch the existing journl entry
            JournalModel existingJournal = journalRepository.findById(journalId)
                    .orElseThrow(() -> {
                        log.warn("Journal entry not found: {}", journalId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
                    });
            //validate if the user ouns the journal entry
            UserModel user = userServices.findByUserName(userName);

            if (user == null || !user.getJournalEntries().contains(existingJournal)) {
                log.warn("Unauthorized delete attempt by user: {}", userName);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your are not authorized to delete this journal");
            }

            // Remove the journal entry from the user's list
            user.getJournalEntries().remove(existingJournal);
            userServices.saveUser(user);

            // Delete the journal entry
            journalRepository.deleteById(journalId);

            log.info("Journal entry deleted successfully: {}", journalId);
            return new ResponseEntity<>("Journal entry deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deleting journal entry: {}", e.getMessage(), e);
            return new ResponseEntity<>("An error occurred while deleting the entry.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

