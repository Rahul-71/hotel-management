package com.telus.hotel_management.controller;

import com.telus.hotel_management.dto.JwtResponse;
import com.telus.hotel_management.dto.LoginRequest;
import com.telus.hotel_management.dto.UserRegistrationDto;
import com.telus.hotel_management.entity.User;
import com.telus.hotel_management.exception.UserAlreadyExistsException;
import com.telus.hotel_management.security.JwtUtils;
import com.telus.hotel_management.service.UserDetailsImpl;
import com.telus.hotel_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void registerUser_Success() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("testuser");
        dto.setPassword("password");
        dto.setEmail("test@example");

        when(userService.registerUser(dto)).thenReturn(new User());

        ResponseEntity<?> response = authController.registerUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully!", response.getBody());
        verify(userService, times(1)).registerUser(dto);
    }

    @Test
    void registerUser_UserAlreadyExists() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("existinguser");
        doThrow(new UserAlreadyExistsException("User exists")).when(userService).registerUser(dto);

        ResponseEntity<?> response = authController.registerUser(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User exists", response.getBody());
    }


    @Test
    void createUser_Success() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("adminuser");
        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.createAdminUser(dto)).thenReturn(mockUser);

        ResponseEntity<?> response = authController.createUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User created successfully with ID: 1", response.getBody());
    }

    @Test
    void authenticateUser_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        when(auth.getPrincipal()).thenReturn(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock-jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertNotNull(jwtResponse);
        assertEquals("mock-jwt-token", jwtResponse.getToken());
        assertEquals(1L, jwtResponse.getId());
        assertEquals("testuser", jwtResponse.getUsername());
    }

    @Test
    void authenticateUser_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setPassword("wrongpass");
        request.setUsername("user");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationException("Bad credentials") {
                });

        ResponseEntity<?> response = authController.authenticateUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password.", response.getBody());
    }
}
