package org.example.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
    private String gender;
}
