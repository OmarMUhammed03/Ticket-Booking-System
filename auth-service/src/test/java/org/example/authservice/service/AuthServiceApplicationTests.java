package org.example.authservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.example.authservice.repository.AuthUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.example.authservice.model.AuthUser;
import org.example.authservice.model.ROLE;
import org.example.authservice.dto.SignupRequest;
import org.example.authservice.dto.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthUserRepository authUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authUserRepository.deleteAll();
    }

    @AfterEach
    void reset() {
        authUserRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(authUserRepository).isNotNull();
        assertThat(passwordEncoder).isNotNull();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void testRegisterUserIntegration() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("integration@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstname("Integration");
        signupRequest.setLastname("Test");
        signupRequest.setGender("Other");
        signupRequest.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        signupRequest.setRole("USER");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Account registered."));

        AuthUser user = authUserRepository.findByEmail("integration@example.com").orElse(null);
        assertThat(user).isNotNull();
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        assertThat(user.getRole()).isEqualTo(ROLE.USER);
    }

    @Test
    void testLoginIntegration() throws Exception {
        AuthUser user = AuthUser.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(ROLE.USER)
                .build();
        authUserRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().exists("X-Refresh-Token"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testLoginFailsWithWrongPassword() throws Exception {
        AuthUser user = AuthUser.builder()
                .email("fail@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(ROLE.USER)
                .build();
        authUserRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("fail@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError()); // because the global handler maps AuthenticationException to 500
    }

    @Test
    void testLogoutIntegration() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("logout@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstname("Logout");
        signupRequest.setLastname("Test");
        signupRequest.setGender("Other");
        signupRequest.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        signupRequest.setRole("USER");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logout@example.com");
        loginRequest.setPassword("password123");
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String authHeader = result.getResponse().getHeader("Authorization");

        String rawAccessToken = authHeader.substring(7);
        Cookie accessTokenCookie = new Cookie("accessToken", rawAccessToken);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", authHeader)
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("You've been signed out!"));
    }

    @Test
    void testRefreshTokenIntegration() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("refresh@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstname("Refresh");
        signupRequest.setLastname("Test");
        signupRequest.setGender("Other");
        signupRequest.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        signupRequest.setRole("USER");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("refresh@example.com");
        loginRequest.setPassword("password123");
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = result.getResponse().getHeader("X-Refresh-Token");

        mockMvc.perform(post("/auth/refresh-token").cookie(new Cookie("refreshToken", refreshToken))
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refreshed tokens successfully")));
    }
}
