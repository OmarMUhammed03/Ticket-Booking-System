package org.example.userservice.controller;

import org.example.userservice.dto.UserResponseDto;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserResponseDto user1;
    private UserResponseDto user2;
    private UUID userId1;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        user1 = new UserResponseDto(userId1, "john.doe@example.com", "John", "Doe", LocalDate.of(1990, 5, 15), null, "Male");
        user2 = new UserResponseDto(userId2, "jane.smith@example.com", "Jane", "Smith", LocalDate.of(1992, 8, 22), null, "Female");
    }

    @Test
    @DisplayName("GET /users - Success")
    void shouldGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[1].firstName", is("Jane")));
    }

    @Test
    @DisplayName("GET /users - Success (Empty List)")
    void shouldGetEmptyListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /users/{id} - Found")
    void shouldGetUserByIdWhenFound() throws Exception {
        when(userService.getUserById(userId1)).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/users/{id}", userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userId1.toString())))
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    @DisplayName("GET /users/{id} - Not Found")
    void shouldReturnNotFoundWhenGetUserById() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getUserById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /users/{id} - Success")
    void shouldDeleteUserWhenFound() throws Exception {
        when(userService.deleteUser(userId1)).thenReturn(Optional.of(user1));

        mockMvc.perform(delete("/users/{id}", userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.id", is(userId1.toString())));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Not Found")
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.deleteUser(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/current-user - Success")
    void shouldGetCurrentUserWithHeader() throws Exception {
        when(userService.getUserById(userId1)).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/users/current-user")
                        .header("X-User-Id", userId1.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userId1.toString())))
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    @DisplayName("GET /users/current-user - Not Found")
    void shouldReturnNotFoundForCurrentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getUserById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/current-user")
                        .header("X-User-Id", nonExistentId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/current-user - Bad Request (Missing Header)")
    void shouldReturnBadRequestWhenCurrentUserHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/users/current-user"))
                .andExpect(status().isBadRequest());
    }
}
