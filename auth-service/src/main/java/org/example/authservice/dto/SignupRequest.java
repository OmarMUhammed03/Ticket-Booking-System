package org.example.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignupRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String firstname;
    @NotBlank
    private String lastname;
    @SuppressWarnings("checkstyle:MagicNumber")
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    @NotBlank
    private String role;
    @NotNull
    private LocalDate dateOfBirth;
    @NotNull
    private String gender;
}

