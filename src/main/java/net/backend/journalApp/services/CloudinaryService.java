package net.backend.journalApp.services;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.repository.JournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map uploadImage(MultipartFile file) {
        try {
            log.info("Uploading image: {}", file.getOriginalFilename());

            Map fileUploadData = this.cloudinary.uploader().upload(file.getBytes(), Map.of());

            log.info("Image uploaded successfully: {}", fileUploadData.get("url"));
            return fileUploadData;
        } catch (IOException e) {
            log.error("Failed to upload image: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload Image on Cloudinary", e);
        }
    }
}

