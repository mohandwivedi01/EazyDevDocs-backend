package net.backend.journalApp.repository;

import net.backend.journalApp.model.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends  MongoRepository<UserModel, Object>{
    UserModel findByUserName(String user);
}