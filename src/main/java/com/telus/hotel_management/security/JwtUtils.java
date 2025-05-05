package com.telus.hotel_management.security;

import com.telus.hotel_management.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${hotelbooking.app.jwtSecret}")
    private String jwtSecret;

    @Value("${hotelbooking.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Correct way to get the Key object from the secret string
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // **Helper method to get a configured JwtParser instance**
    // Building the parser can be slightly resource-intensive, so reusing it is good practice.
    // You could make this a @Bean if used very frequently across multiple classes.
    private JwtParser jwtParser() {
        return Jwts.parser()
                .setSigningKey(key()) // Set the signing key
                // .requireSubject("...") // Add any required claims here
                .build(); // Build the parser instance
    }


    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // Set the subject claim
                .setIssuedAt(new Date()) // Set the issued at time
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Set the expiration time
                .signWith(key(), SignatureAlgorithm.HS256) // Sign the token
                .compact(); // Build and compact the token
    }

    // **Corrected getUserNameFromJwtToken for JJWT 0.10.0+**
    // This method needs to use the JwtParser instance to call parseClaimsJws
    public String extractUsername(String token) {
        // Use the built parser from the helper method
        // parseClaimsJws returns a Jws<Claims> object
        Jws<Claims> claimsJws = jwtParser().parseClaimsJws(token); // <--- Call parseClaimsJws on the JwtParser
        return claimsJws.getBody().getSubject(); // Get claims from the body and then the subject
    }

    // **Corrected validateJwtToken for JJWT 0.10.0+**
    // This method needs to use the JwtParser instance to call parseClaimsJws
    public boolean validateJwtToken(String authToken) {
        try {
            // Use the built parser to validate and parse the JWS with claims
            jwtParser().parseClaimsJws(authToken); // <--- Call parseClaimsJws on the JwtParser
            return true; // Token is valid and parsed successfully
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false; // Token validation failed
    }
}