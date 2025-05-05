//package com.telus.hotel_management.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//public class JwtUtils {
//    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//
//    @Value("${hotelbooking.app.jwtSecret}")
//    private String jwtSecret;
//
//    @Value("${hotelbooking.app.jwtExpirationMs}")
//    private long jwtExpirationMs;
//
//    public String extractUsername(String token) {
//        if (token == null) {
//            logger.error("Token is null");
//            throw new JwtException("Token is null");
//        }
//        logger.info("Extracting username from token: {}", token);
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public Date extractExpiration(String token) {
//        if (token == null) {
//            logger.error("Token is null");
//            throw new JwtException("Token is null");
//        }
//        logger.info("Extracting expiration date from token: {}", token);
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        if (token == null) {
//            logger.error("Token is null");
//            throw new JwtException("Token is null");
//        }
//        logger.info("Extracting claim from token: {}", token);
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    private Claims extractAllClaims(String token) {
//        if (token == null) {
//            logger.error("Token is null");
//            throw new JwtException("Token is null");
//        }
//        logger.info("Extracting all claims from token: {}", token);
//        try {
//            return Jwts.parser()
//                    .setSigningKey(jwtSecret)
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (JwtException e) {
//            logger.error("Error parsing token: {}", token, e);
//            throw e;
//        }
//    }
//
//    private Boolean isTokenExpired(String token) {
//        if (token == null) {
//            logger.error("Token is null");
//            throw new JwtException("Token is null");
//        }
//        logger.info("Checking if token is expired: {}", token);
//        return extractExpiration(token).before(new Date());
//    }
//
//    public String generateToken(UserDetails userDetails) {
//        if (userDetails == null) {
//            logger.error("User details are null");
//            throw new IllegalArgumentException("User details are null");
//        }
//        logger.info("Generating token for user: {}", userDetails.getUsername());
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, userDetails.getUsername());
//    }
//
//    private String createToken(Map<String, Object> claims, String subject) {
//        if (subject == null) {
//            logger.error("Subject is null");
//            throw new IllegalArgumentException("Subject is null");
//        }
//        logger.info("Creating token with subject: {}", subject);
//        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
//                .signWith(SignatureAlgorithm.HS256, jwtSecret).compact();
//    }
//
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        if (token == null || userDetails == null) {
//            logger.error("Token or user details are null");
//            throw new JwtException("Token or user details are null");
//        }
//        logger.info("Validating token for user: {}", userDetails.getUsername());
//        final String username = extractUsername(token);
//        if (!username.equals(userDetails.getUsername())) {
//            logger.error("Token username does not match user details username: {}", token);
//            return false;
//        }
//        if (isTokenExpired(token)) {
//            logger.error("Token is expired: {}", token);
//            return false;
//        }
//        logger.info("Token is valid: {}", token);
//        return true;
//    }
//}