package org.example.authservice.controller;

import org.example.authservice.config.SecurityConfig;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.SignupRequest;
import org.example.authservice.dto.LoginResponse;
import org.example.authservice.service.AuthService;
import org.example.authservice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should authenticate and return tokens on valid login")
    void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        LoginResponse loginResponse = new LoginResponse("access-token", "refresh-token");
        Mockito.when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer access-token"))
                .andExpect(header().string("X-Refresh-Token", "refresh-token"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Should register a new user and return confirmation message")
    void testRegisterUser() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstname("New");
        signupRequest.setLastname("User");
        signupRequest.setGender("Other");
        signupRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        signupRequest.setRole("USER");

        doNothing().when(authenticationService).registerAccount(any(SignupRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Account registered."));
    }

    @Test
    @WithMockUser
    @DisplayName("Should logout user and return sign out message")
    void testLogoutUser() throws Exception {
        doNothing().when(authenticationService).logoutUser(any(), any());
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("You've been signed out!"));
    }
}
