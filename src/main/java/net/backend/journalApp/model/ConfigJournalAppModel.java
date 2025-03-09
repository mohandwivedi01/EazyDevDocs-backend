package net.backend.journalApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@NoArgsConstructor
public class ConfigJournalAppModel{

    private String key;
    private String value;
}