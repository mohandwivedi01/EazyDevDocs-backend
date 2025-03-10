package net.backend.journalApp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private  String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                List<String> originsList = Arrays.asList(allowedOrigins.split(","));
                List<String> methodsList = Arrays.asList(allowedMethods.split(","));

                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins(originsList.toArray(new String[0])) // Allow frontend
                        .allowedMethods(methodsList.toArray(new String[0]))
                        .allowedHeaders(allowedHeaders)
                        .allowCredentials(allowCredentials);
            }
        };
    }
}
