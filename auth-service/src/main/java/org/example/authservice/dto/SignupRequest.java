package org.example.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String name;
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    @NotBlank
    private String role;
}

