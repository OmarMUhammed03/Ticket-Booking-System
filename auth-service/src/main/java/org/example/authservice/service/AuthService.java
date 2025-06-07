package org.example.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.CustomUserDetails;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.SignupRequest;
import org.example.authservice.exception.InvalidActionException;
import org.example.authservice.exception.ValidationException;
import org.example.authservice.model.AuthUser;
import org.example.authservice.model.ROLE;
import org.example.authservice.repository.AuthUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final JwtService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final String[] roles = {
            ROLE.ADMIN.toString(),
            ROLE.USER.toString(),
            ROLE.ORGANIZER.toString()
    };

    public String login(final LoginRequest loginRequest,
                        final HttpServletResponse response) {
        checkUserLoggedIn("User already logged in.");

        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.
                        unauthenticated(
                                loginRequest.getEmail(),
                                loginRequest.getPassword());
        Authentication authenticationResponse =
                this.authenticationManager.authenticate(authenticationRequest);

        SecurityContextHolder.getContext().
                setAuthentication(authenticationResponse);
        tokenService.generateToken(loginRequest.getEmail(), response);
        tokenService.generateRefreshToken(loginRequest.getEmail(), response);
        UserDetails userDetails =
                (UserDetails) authenticationResponse.getPrincipal();

        return userDetails.getUsername();
    }

    public void registerAccount(final SignupRequest signupRequest) {
        checkUserLoggedIn("User cannot register while logged in.");

        if (authUserRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ValidationException("Email already in use.");
        }

        if (!Arrays.stream(roles).toList().contains(signupRequest.getRole())) {
            throw new ValidationException("Invalid role provided.");
        }


        AuthUser user = AuthUser.builder()
                .email(signupRequest.getEmail())
                .password(
                        passwordEncoder.encode(
                                signupRequest.getPassword()))
                .role(ROLE.valueOf(signupRequest.getRole()))
                .build();
        authUserRepository.save(user);
    }

    public void logoutUser(
            final HttpServletResponse response) {
        tokenService.removeAccessTokenFromCookie(response);
        tokenService.removeRefreshTokenFromCookieAndExpire(response);
    }

    public String refreshToken(final HttpServletRequest request,
                               final HttpServletResponse response) {
        try {
            String refreshToken =
                    tokenService.getRefreshTokenFromCookie(request);
            checkNotNullAccessDeniedException(refreshToken,
                    "Refresh token cannot be found.");
            String email =
                    tokenService.renewRefreshToken(refreshToken, response);
            tokenService.generateToken(email, response);
            return email;
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid refresh token.");
        }
    }

    public void addUserDetailsInHeader(
            final HttpServletResponse response) {

        CustomUserDetails userDetails = getCurrentUserDetails();
        String email = userDetails.getUsername();
        StringBuilder roles = new StringBuilder();
        userDetails.getAuthorities().forEach((authority) -> {
            if (!roles.isEmpty()) {
                roles.append(", ");
            }
            roles.append(authority.getAuthority());
        });
        setRoleAndEmailInHeader(response, email, roles.toString(),
                userDetails.getId().toString());
    }

    private void setRoleAndEmailInHeader(
            final HttpServletResponse response,
            final String email,
            final String roles,
            final String id) {
        response.setHeader("X-User-Email", email);
        response.setHeader("X-User-Roles", roles);
        response.setHeader("X-User-Id", id);
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        } else {
            throw new InvalidActionException(
                    "User details not saved correctly.");
        }
    }

    private void checkUserLoggedIn(final String message) {
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof UsernamePasswordAuthenticationToken) {
            throw new InvalidActionException(message);
        }
    }

    private void checkNotNullAccessDeniedException(final Object value,
                                                   final String message) {
        if (value == null) {
            throw new AccessDeniedException(message);
        }
    }
}
