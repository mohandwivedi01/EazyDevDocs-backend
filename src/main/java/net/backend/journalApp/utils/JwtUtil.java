package net.backend.journalApp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${secret.api.key}")
    private String SECRET_KEY;


    // ✅ Set expiration to 24 hours
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private SecretKey getSigningKey() {
        log.debug("Generating signing key for JWT...");
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String extractUsername(String token) {
        log.info("Extracting username from token...");
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        log.info("Extracting expiration date from token...");
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim from token...");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        log.info("Parsing JWT to extract claims...");
        try {
            return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            log.error("Failed to extract claims from token. Invalid token: {}", token, e);
            throw e;
        }
    }

    private boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        if (expired) {
            log.warn("Token is expired: {}", token);
        } else {
            log.info("Token is still valid.");
        }
        return expired;
    }

    public String generateToken(String username) {
        log.info("Generating new JWT for user: {}", username);
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, username);
        log.info("Generated JWT successfully for user: {}", username);
        return token;
    }



    private String createToken(Map<String, Object> claims, String subject) {
        log.debug("Creating JWT with claims and subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // ✅ Token valid for 24 hours
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        log.info("Validating token for user: {}", userDetails.getUsername());
        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

        if (isValid) {
            log.info("Token validation successful for user: {}", username);
        } else {
            log.warn("Token validation failed for user: {}", username);
        }

        return isValid;
    }


}


