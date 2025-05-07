package com.telus.hotel_management.controller;

import com.telus.hotel_management.dto.JwtResponse;
import com.telus.hotel_management.dto.LoginRequest;
import com.telus.hotel_management.dto.UserRegistrationDto;
import com.telus.hotel_management.entity.User;
import com.telus.hotel_management.exception.UserAlreadyExistsException;
import com.telus.hotel_management.security.JwtUtils;
import com.telus.hotel_management.service.UserDetailsImpl;
import com.telus.hotel_management.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("Registering new user with username: {}", registrationDto.getUsername());
        try {
            userService.registerUser(registrationDto);
            log.info("User registered successfully with username: {}", registrationDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (UserAlreadyExistsException e) {
            log.error("User already exists with username: {}", registrationDto.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error registering user with username: {}", registrationDto.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getLocalizedMessage());
        }
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRegistrationDto userDto) {
        log.info("Admin received request to create user with username: {}", userDto.getUsername());
        try {
            User createdUser = userService.createAdminUser(userDto);
            log.info("Admin successfully created user with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully with ID: " + createdUser.getId());
        } catch (Exception e) {
            log.error("Admin failed to create user with username: {}", userDto.getUsername(), e);
            throw e;
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Authenticating user with username: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            log.info("User authenticated successfully with username: {}", loginRequest.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.debug("User details: {}", userDetails);

            String jwt = jwtUtils.generateJwtToken(authentication);
            log.debug("Generated JWT token: {}", jwt);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            log.debug("User roles: {}", roles);

            log.info("Returning JWT response for user with username: {}", loginRequest.getUsername());
            JwtResponse jwtResponse = new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    roles);
            log.info("JWT response: {}", jwtResponse);
            return ResponseEntity.ok(jwtResponse);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user with username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }
    }


}