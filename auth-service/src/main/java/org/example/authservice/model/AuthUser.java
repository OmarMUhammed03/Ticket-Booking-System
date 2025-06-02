package org.example.authservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.userservice.model.User;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "authUsers")
public class AuthUser {
    @Id
    private UUID userId;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    private ROLE role;
    private Set<UUID> refreshTokens;
    @OneToOne
    @MapsId
    @JoinColumn(name = "userId")
    private User user;
}

