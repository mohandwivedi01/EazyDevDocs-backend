package net.backend.journalApp.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${CLOUDINARY.CLOUD_NAME}")
    private String cloudName;

    @Value("${CLOUDINARY.API_KEY}")
    private String apiKey;

    @Value("${CLOUDINARY.API_SECRET}")
    private String apiSecret;
    @Bean
    public Cloudinary cloudinary(){
        Map cloudinaryConfig = new HashMap<>();
        cloudinaryConfig.put("cloud_name", cloudName);
        cloudinaryConfig.put("api_key", apiKey);
        cloudinaryConfig.put("api_secret", apiSecret);
        cloudinaryConfig.put("secure", true);

        return new Cloudinary(cloudinaryConfig);

    }
}
