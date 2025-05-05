package com.telus.hotel_management.service;

import com.telus.hotel_management.dto.UserRegistrationDto;
import com.telus.hotel_management.entity.Role;
import com.telus.hotel_management.entity.RoleType;
import com.telus.hotel_management.entity.User;
import com.telus.hotel_management.exception.InvalidRoleException;
import com.telus.hotel_management.exception.ResourceNotFoundException;
import com.telus.hotel_management.exception.UserAlreadyExistsException;
import com.telus.hotel_management.repository.RoleRepository;
import com.telus.hotel_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    //   register new user
    @Transactional
    public User registerUser(UserRegistrationDto userDto) {
        String username = userDto.getUsername();
        String rawPassword = userDto.getPassword(); // Get the raw password from the DTO

        log.info("Registering new user with username: {}", username);
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("User with username {} already exists", username);
            throw new UserAlreadyExistsException("User with username " + username + " already exists");
        }

        User user = modelMapper.map(userDto, User.class);
// Take the raw password you got from the DTO and encode it
        String encodedPassword = encoder.encode(rawPassword);
        // Set the encoded password on the User entity
        user.setPassword(encodedPassword);
        log.info("User created: {}", user);

        log.info("User created (username only): {}", user.getUsername()); // Log username to avoid exposing password data


        // assign role to user (Your existing logic for assigning USER role)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> {
                    log.error("User role not found");
                    return new ResourceNotFoundException("User role not found");
                });
        roles.add(userRole);
        user.setRoles(roles);

        log.info("Roles assigned to user: {}", user.getRoles());

        User savedUser = userRepository.save(user);
        log.info("User saved: {}", savedUser);

        log.info("User saved successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Transactional
    public User createAdminUser(UserRegistrationDto userDto) {
        String username = userDto.getUsername();
        String rawPassword = userDto.getPassword();

        log.info("Admin creating new user with username: {}", username);
        if (userRepository.findByUsername(username).isPresent()) {
            log.error("User with username {} already exists (Admin creation)", username);
            throw new UserAlreadyExistsException("User with username " + username + " already exists");
        }

        User user = modelMapper.map(userDto, User.class);
        user.setPassword(encoder.encode(rawPassword));

        Set<String> roleNames = userDto.getRoles();
        Set<Role> roles = new HashSet<>();

        if (roleNames != null && !roleNames.isEmpty()) {
            roles = roleNames.stream()
                    .map(roleName -> {
                        try {
                            RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
                            return roleRepository.findByName(roleType)
                                    .orElseThrow(() -> {
                                        log.error("Role '{}' not found in database during creation", roleName);
                                        return new ResourceNotFoundException("Role not found: " + roleName);
                                    });
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid role name provided during admin creation: {}", roleName, e);
                            throw new InvalidRoleException("Invalid role name: " + roleName);
                        }
                    })
                    .collect(Collectors.toSet());
        }

        if (roles.isEmpty()) {
            log.error("User '{}' created by admin with no valid roles assigned.", username);
            throw new InvalidRoleException("User must have at least one valid role.");
        }


        user.setRoles(roles);

        log.info("Admin saving new user with roles: {}", user.getUsername());
        User savedUser = userRepository.save(user);
        log.info("User created by admin and saved with ID: {}", savedUser.getId());

        return savedUser;
    }

    //    find user by username
//    public User findByUsername(String username) {
//        log.info("Finding user by username: {}", username);
//        return userRepository.findByUsername(username)
//                .orElseThrow(() -> {
//                    log.error("User not found with username: {}", username);
//                    return new ResourceNotFoundException("User not found with username : " + username);
//                });
//    }

    //    fing user by id
    public User findById(Long id) {
        log.info("Finding user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id : " + id);
                });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
        log.info("User found: {}", user);
        return new UserDetailsImpl(user);
    }

//    //  for admin:  method to assing roles
//    public User assignRoleToUser(Long userId, RoleType roleName) {
//        log.info("Assigning role to user with id: {} and role: {}", userId, roleName);
//        User user = findById(userId);
//        Role role = roleRepository.findByName(roleName)
//                .orElseThrow(() -> {
//                    log.error("Role not found with name: {}", roleName);
//                    return new ResourceNotFoundException("Role not found with name : " + roleName);
//                });
//        user.getRoles().add(role);
//        log.info("Role assigned to user: {}", user.getRoles());
//
//        User savedUser = userRepository.save(user);
//        log.info("User saved: {}", savedUser);
//
//        return savedUser;
//    }
}
