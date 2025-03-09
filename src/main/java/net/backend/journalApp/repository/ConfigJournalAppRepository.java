package net.backend.journalApp.repository;

import net.backend.journalApp.model.ConfigJournalAppModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ConfigJournalAppRepository extends MongoRepository<ConfigJournalAppModel, ObjectId> {

}