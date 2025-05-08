//package com.telus.hotel_management.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.telus.hotel_management.dto.LoginRequest;
//import com.telus.hotel_management.dto.UserRegistrationDto;
//import com.telus.hotel_management.entity.Role;
//import com.telus.hotel_management.entity.RoleType;
//import com.telus.hotel_management.entity.User;
//import com.telus.hotel_management.exception.UserAlreadyExistsException;
//import com.telus.hotel_management.security.JwtUtils;
//import com.telus.hotel_management.service.UserDetailsImpl;
//import com.telus.hotel_management.service.UserService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.hamcrest.Matchers.containsString;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class AuthControllerTests {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//    @MockBean
//    private AuthenticationManager authenticationManager;
//
//    @MockBean
//    private UserService userService;
//
//    @MockBean
//    private JwtUtils jwtUtils;
//
//    @Test
//    void testRegisterUser_success() throws Exception {
//        UserRegistrationDto registrationDto = new UserRegistrationDto();
//        registrationDto.setUsername("testuser");
//        registrationDto.setPassword("securepassword");
//        registrationDto.setEmail("test@example.com");
//
//        User mockRegisteredUser = new User();
//        mockRegisteredUser.setId(1L);
//        mockRegisteredUser.setUsername("testuser");
//
//
//        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(mockRegisteredUser);
//
//        mockMvc.perform(post("/api/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registrationDto)))
//                .andExpect(status().isCreated())
//                .andExpect(content().string(containsString("User registered successfully!")));
//
//        verify(userService).registerUser(any(UserRegistrationDto.class));
//    }
//
//    @Test
//    void testRegisterUser_userExists() throws Exception {
//        UserRegistrationDto registrationDto = new UserRegistrationDto();
//        registrationDto.setUsername("existinguser");
//        registrationDto.setPassword("password123");
//        registrationDto.setEmail("existing@example.com");
//
//        when(userService.registerUser(any(UserRegistrationDto.class)))
//                .thenThrow(new UserAlreadyExistsException("User with username existinguser already exists"));
//
//        mockMvc.perform(post("/api/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registrationDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string(containsString("User with username existinguser already exists")));
//
//        verify(userService).registerUser(any(UserRegistrationDto.class));
//    }
//
//    @Test
//    void testRegisterUser_validationError() throws Exception {
//        UserRegistrationDto registrationDto = new UserRegistrationDto();
//        registrationDto.setUsername("");
//        registrationDto.setPassword("short");
//        registrationDto.setEmail("invalid-email-format");
//
//
//        mockMvc.perform(post("/api/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registrationDto)))
//                .andExpect(status().isBadRequest());
//
//
//        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
//    }
//
//    private UserDetailsImpl createMockUserDetails(Long id, String username, String password, List<String> authorities) {
//        User user = new User();
//        user.setId(id);
//        user.setUsername(username);
//        user.setPassword(password);
//
//        user.setRoles(authorities.stream()
//                .map(role -> new Role(RoleType.valueOf(role)))
//                .collect(Collectors.toSet()));
//
//        return new UserDetailsImpl(user);
//    }
//
//    @Test
//    void testAuthenticateUser_invalidCredentials() throws Exception {
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setUsername("wronguser");
//        loginRequest.setPassword("wrongpassword");
//
//        when(authenticationManager.authenticate(any(Authentication.class)))
//                .thenThrow(new BadCredentialsException("Invalid username or password."));
//
//        mockMvc.perform(post("/api/signin")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(content().string(containsString("Invalid username or password.")));
//
//        verify(authenticationManager).authenticate(any(Authentication.class));
//        verify(jwtUtils, never()).generateJwtToken(any(Authentication.class));
//    }
//
//    @Test
//    void testAuthenticateUser_validationError() throws Exception {
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setUsername("");
//        loginRequest.setPassword(null);
//
//        mockMvc.perform(post("/api/signin")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isBadRequest());
//
//        verify(authenticationManager, never()).authenticate(any(Authentication.class));
//        verify(jwtUtils, never()).generateJwtToken(any(Authentication.class));
//    }
//
//
//    @Test
//    @WithMockUser(authorities = "ADMIN")
//    void testCreateUser_adminSuccess() throws Exception {
//        UserRegistrationDto userDto = new UserRegistrationDto();
//        userDto.setUsername("admincreateduser");
//        userDto.setPassword("adminpassword");
//        userDto.setEmail("admincreate@example.com");
//
//        User mockCreatedUser = new User();
//        mockCreatedUser.setId(100L);
//        mockCreatedUser.setUsername("admincreateduser");
//
//        when(userService.createAdminUser(any(UserRegistrationDto.class)))
//                .thenReturn(mockCreatedUser);
//
//        mockMvc.perform(post("/api/admin/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userDto)))
//                .andExpect(status().isCreated())
//                .andExpect(content().string(containsString("User created successfully with ID: 100")));
//
//        verify(userService).createAdminUser(any(UserRegistrationDto.class));
//    }
//
//    @Test
//    @WithMockUser(authorities = "USER")
//    void testCreateUser_forbiddenForNonAdmin() throws Exception {
//        UserRegistrationDto userDto = new UserRegistrationDto();
//        userDto.setUsername("someuser");
//        userDto.setPassword("somepassword");
//        userDto.setEmail("some@example.com");
//
//        mockMvc.perform(post("/api/admin/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userDto)))
//                .andExpect(status().isForbidden());
//
//        verify(userService, never()).createAdminUser(any(UserRegistrationDto.class));
//    }
//
//    @Test
//    void testCreateUser_unauthenticated() throws Exception {
//
//        UserRegistrationDto userDto = new UserRegistrationDto();
//        userDto.setUsername("someuser");
//        userDto.setPassword("somepassword");
//        userDto.setEmail("some@example.com");
//
//
//        mockMvc.perform(post("/api/admin/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userDto)))
//                .andExpect(status().isForbidden());
//
//        verify(userService, never()).createAdminUser(any(UserRegistrationDto.class));
//    }
//}