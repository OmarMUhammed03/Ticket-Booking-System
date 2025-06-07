package org.example.authservice.repository;

import org.example.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByUserIdAndExpiresAtAfter(UUID userId, Instant date);
    Optional<RefreshToken> findByIdAndExpiresAtAfter(UUID id, Instant date);
}
