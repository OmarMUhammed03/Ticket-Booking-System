package org.example.userservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDto(UUID id, String email, String firstName, String lastName, LocalDate dateOfBirth,
                              String profilePictureUrl, String gender) {
}