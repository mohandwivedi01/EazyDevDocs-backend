package net.backend.journalApp.cache;

import net.backend.journalApp.model.ConfigJournalAppModel;
import net.backend.journalApp.repository.ConfigJournalAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The `AppCache` class is responsible for initializing and managing a cache
 * for application configuration settings. These settings are fetched from
 * the database and stored in memory for quick access throughout the application.
 */
@Component // Marks this class as a Spring-managed component for dependency injection.
public class AppCache {

    @Autowired
    private ConfigJournalAppRepository configJournalAppRepository; // Repository for accessing configuration data from the database.

    public Map<String, String> APP_CACHE; // In-memory cache to store key-value pairs of configuration data.

    /**
     * This method initializes the application cache. It is executed automatically
     * after the bean is constructed (PostConstruct lifecycle event).
     */
    @PostConstruct
    public void init() {
        APP_CACHE = new HashMap<>(); // Initialize the in-memory cache as a HashMap.

        // Fetch all configuration settings from the database.
        List<ConfigJournalAppModel> all = configJournalAppRepository.findAll();

        // Populate the cache with key-value pairs from the database.
        for (ConfigJournalAppModel configJournalAppModel : all) {
            APP_CACHE.put(configJournalAppModel.getKey(), configJournalAppModel.getValue());
        }
    }
}
