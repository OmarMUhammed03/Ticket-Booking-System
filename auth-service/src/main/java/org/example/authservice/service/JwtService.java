package org.example.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.authservice.exception.ValidationException;
import org.example.authservice.model.AuthUser;
import org.example.authservice.model.RefreshToken;
import org.example.authservice.repository.AuthUserRepository;
import org.example.authservice.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class JwtService {
    private final AuthUserRepository authUserRepository;
    private final TokenRepository refreshTokenRepository;

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expires}")
    private Long jwtExpiresMinutes;

    private static final Integer ACCESS_TOKEN_EXPIRATION_TIME = 60 * 1000;
    private static final Integer ACCESS_TOKEN_COOKIE_EXPIRATION_TIME = 60 * 30;
    private static final TemporalAmount REFRESH_TOKEN_EXPIRATION_TIME =
            java.time.Duration.ofDays(10);
    private static final Integer REFRESH_TOKEN_COOKIE_EXPIRATION_TIME =
            60 * 60 * 24 * 10;
    private static final String REFRESH_TOKEN_COOKIE_PATH =
            "/api/auth/refresh-token";
    private static final String ACCESS_TOKEN_COOKIE_PATH = "/";

    public String extractEmail(Claims claims) {
        return claims.getSubject();
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiresMinutes * ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(getSignInKey())
                .compact();
    }

    public String generateRefreshToken(final String email) {
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));
        RefreshToken userRefreshToken =
                refreshTokenRepository.findByUserIdAndExpiresAtAfter(
                        user.getId(), Instant.now()).orElse(null);

        if (userRefreshToken == null) {
            userRefreshToken =
                    RefreshToken.builder()
                            .user(user)
                            .createdAt(Instant.now())
                            .expiresAt(Instant.now().plus(
                                    REFRESH_TOKEN_EXPIRATION_TIME)).build();
            refreshTokenRepository.save(userRefreshToken);
        }

        return userRefreshToken.getId().toString();
    }

    public Claims validateAccessToken(final String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public String getRefreshTokenFromCookie(final HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    private void addRefreshTokenToCookie(final String refreshToken,
                                         final HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(REFRESH_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_EXPIRATION_TIME);
        response.addCookie(cookie);
    }

    public String renewRefreshToken(final String refreshToken,
                                    final HttpServletResponse response) {
        RefreshToken token =
                refreshTokenRepository
                        .findByIdAndExpiresAtAfter(
                                UUID.fromString(refreshToken), Instant.now())
                        .orElseThrow(() ->
                                new ValidationException(
                                        "Invalid refresh token"));

        token.setExpiresAt(Instant.now());
        refreshTokenRepository.save(token);

        RefreshToken newRefreshToken =
                RefreshToken.builder()
                        .user(token.getUser())
                        .createdAt(Instant.now())
                        .expiresAt(Instant.now().plus(
                                REFRESH_TOKEN_EXPIRATION_TIME)).build();
        refreshTokenRepository.save(newRefreshToken);

        String newRefreshTokenString = newRefreshToken.getId().toString();
        addRefreshTokenToCookie(newRefreshTokenString, response);

        return token.getUser().getEmail();
    }

    public void removeAccessTokenFromCookie(
            final HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath(ACCESS_TOKEN_COOKIE_PATH);

        response.addCookie(cookie);
    }

    public void removeRefreshTokenFromCookieAndExpire(
            final HttpServletResponse response, String token) {
        Claims claims = validateAccessToken(token);
        String userEmail = extractEmail(claims);
        AuthUser user =
                authUserRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new ValidationException(
                                "User not found"));
        RefreshToken refreshToken =
                refreshTokenRepository.findByUserIdAndExpiresAtAfter(
                        user.getId(), Instant.now()).orElseThrow(
                        () -> new ValidationException(
                                "Refresh token not found"));
        expireRefreshToken(refreshToken);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath(REFRESH_TOKEN_COOKIE_PATH);

        response.addCookie(cookie);
    }

    private void expireRefreshToken(final RefreshToken token) {
        token.setExpiresAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(validateAccessToken(token));
        return (tokenEmail.equals(email)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return validateAccessToken(token).getExpiration();
    }
}
