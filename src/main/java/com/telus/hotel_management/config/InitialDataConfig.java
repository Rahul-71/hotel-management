package com.telus.hotel_management.config;

import com.telus.hotel_management.entity.Role;
import com.telus.hotel_management.entity.RoleType;
import com.telus.hotel_management.entity.User;
import com.telus.hotel_management.repository.RoleRepository;
import com.telus.hotel_management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class InitialDataConfig {

    private static final Logger log = LoggerFactory.getLogger(InitialDataConfig.class);

    @Bean
    public CommandLineRunner initializeDatabaseData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("--- Starting Database Initialization ---");
            initializRoleTypes(roleRepository);
            createDefaultAdminUser(userRepository, roleRepository, passwordEncoder);
            log.info("--- Database Initialization Complete ---");
        };
    }

    private void initializRoleTypes(RoleRepository roleRepository) {
        log.info("--- Initializing Roles ---");
        if (roleRepository.findByName(RoleType.USER).isEmpty()) {
            roleRepository.save(new Role(RoleType.USER));
            log.info("Created USER");
        }
        if (roleRepository.findByName(RoleType.HOTEL).isEmpty()) {
            roleRepository.save(new Role(RoleType.HOTEL));
            log.info("Created HOTEL");
        }
        if (roleRepository.findByName(RoleType.ADMIN).isEmpty()) {
            roleRepository.save(new Role(RoleType.ADMIN));
            log.info("Created ADMIN");
        }
        log.info("--- Roles Initialization Complete ---");
    }

    private void createDefaultAdminUser(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        log.info("--- Creating Default Admin User ---");
        String adminUsername = "admin";
        String adminPassword = "admin@123";
        String adminEmail = "admin@hotel.com";

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Admin user '{}' not found. Creating...", adminUsername);

            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setEmail(adminEmail);

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found after initialization!"));
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            log.info("Default Admin user '{}' created successfully.", adminUsername);
            log.info("Admin Username: {}", adminUsername);
            log.info("Admin Password: {}", adminPassword);
        } else {
            log.info("Admin user '{}' already exists. Skipping creation.", adminUsername);
        }
        log.info("--- Default Admin User Check Complete ---");
    }
}