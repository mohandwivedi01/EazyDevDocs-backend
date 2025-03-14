package net.backend.journalApp.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document // Marks this class as a MongoDB document. It will be stored in a collection named "user" by default.
@Data   // Lombok annotation that generates getters, setters, toString, equals, and hashCode methods.
@NoArgsConstructor  // Lombok annotation that generates a no-argument constructor for the class.
public class UserModel {
    @Id
    private String id;  // Unique identifier for each user, automatically generated by MongoDB.

    @NonNull
    @Indexed(unique = true) // Ensures the username is unique in the database.
    private String userName;  // The user's username, which is required and must be unique.

    @NonNull
    private String password;  // The user's password. It's marked as non-null, indicating it is a required field.

    @DBRef // Represents a reference to other MongoDB documents, in this case, the user's journal entries.
    private List<JournalModel> journalEntries = new ArrayList<>();  // List of journal entries associated with the user.

    private List<String> roles;  // List of roles associated with the user (e.g., "USER", "ADMIN").

    @CreatedDate // Automatically sets the value of createdAt when the document is first saved to MongoDB.
    private LocalDateTime createdAt;  // Timestamp for when the user was created.


    @LastModifiedDate // Automatically updates the value of updatedAt whenever the document is modified.
    private LocalDateTime updatedAt;  // Timestamp for when the user was last updated.
}
