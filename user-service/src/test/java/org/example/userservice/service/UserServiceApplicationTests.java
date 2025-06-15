package org.example.userservice.service;

import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void reset() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void testGetAllUsersIntegration() throws Exception {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setEmail("user1@example.com");
        user1.setFirstname("User");
        user1.setLastname("One");
        user1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user1.setGender("Male");
        userRepository.save(user1);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFirstname("User");
        user2.setLastname("Two");
        user2.setDateOfBirth(LocalDate.of(1992, 2, 2));
        user2.setGender("Female");
        userRepository.save(user2);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));
    }

    @Test
    void testGetUserByIdIntegration() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("findme@example.com");
        user.setFirstname("Find");
        user.setLastname("Me");
        user.setDateOfBirth(LocalDate.of(1985, 5, 5));
        user.setGender("Other");
        userRepository.save(user);

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("findme@example.com"));
    }

    @Test
    void testGetUserByIdNotFoundIntegration() throws Exception {
        mockMvc.perform(get("/users/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserIntegration() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("delete@example.com");
        user.setFirstname("Delete");
        user.setLastname("Me");
        user.setDateOfBirth(LocalDate.of(1980, 3, 3));
        user.setGender("Male");
        userRepository.save(user);

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("delete@example.com"));
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void testDeleteUserNotFoundIntegration() throws Exception {
        mockMvc.perform(delete("/users/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCurrentUserIntegration() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("current@example.com");
        user.setFirstname("Current");
        user.setLastname("User");
        user.setDateOfBirth(LocalDate.of(1995, 7, 7));
        user.setGender("Female");
        userRepository.save(user);

        mockMvc.perform(get("/users/current-user").header("X-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("current@example.com"));
    }
}
