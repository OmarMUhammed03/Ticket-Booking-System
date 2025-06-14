package org.example.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateUserDto {
    @NotNull
    private UUID id;
    @NotNull
    private String email;
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @NotNull
    private LocalDate dateOfBirth;
    @NotNull
    private String gender;
    private String profilePictureUrl;

    public CreateUserDto() {

    }
}
