package org.example.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;
    @Column(unique = true)
    private String email;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePictureUrl;

    public boolean isValid() {
        return id != null &&
                email != null && !email.isBlank()
                && firstname != null && !firstname.isBlank()
                && lastname != null && !lastname.isBlank()
                && dateOfBirth != null
                && gender != null && !gender.isBlank();
    }

}

