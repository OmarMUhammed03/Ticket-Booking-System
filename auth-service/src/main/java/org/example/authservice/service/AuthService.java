package org.example.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.LoginResponse;
import org.example.authservice.dto.SignupRequest;
import org.example.commonlibrary.InvalidActionException;
import org.example.commonlibrary.ValidationException;
import org.example.authservice.model.AuthUser;
import org.example.authservice.model.ROLE;
import org.example.authservice.repository.AuthUserRepository;
import org.example.commonlibrary.kafka.MessageProducer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final JwtService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final MessageProducer messageProducer;

    private final String[] roles = {
            ROLE.ADMIN.toString(),
            ROLE.USER.toString(),
            ROLE.ORGANIZER.toString()
    };

    public LoginResponse login(final LoginRequest loginRequest) {
        checkUserLoggedIn("User already logged in.");

        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.
                        unauthenticated(
                                loginRequest.getEmail(),
                                loginRequest.getPassword());
        Authentication authenticationResponse =
                this.authenticationManager.authenticate(authenticationRequest);

        SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
        String accessToken = tokenService.generateToken(loginRequest.getEmail());
        String refreshToken = tokenService.generateRefreshToken(loginRequest.getEmail());
        return new LoginResponse(accessToken, refreshToken);
    }

    public void registerAccount(final SignupRequest signupRequest) {
        checkUserLoggedIn("User cannot register while logged in.");

        if (authUserRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ValidationException("Email already in use.");
        }

        if (!Arrays.stream(roles).toList().contains(signupRequest.getRole())) {
            throw new ValidationException("Invalid role provided.");
        }

        if (signupRequest.getRole().equals("ADMIN")) {
            AuthUser currentUser = getCurrentUserDetails();
            if (!currentUser.getRole().equals(ROLE.ADMIN)) {
                throw new InvalidActionException("Only admin can register another admin.");
            }
        }

        AuthUser user = AuthUser.builder()
                .email(signupRequest.getEmail())
                .password(
                        passwordEncoder.encode(
                                signupRequest.getPassword()))
                .role(ROLE.valueOf(signupRequest.getRole()))
                .build();

        AuthUser savedUser = authUserRepository.save(user);

        messageProducer.sendMessage("user-created", new HashMap<>(Map.of(
                "id", savedUser.getId().toString(),
                "email", user.getEmail(),
                "firstname", signupRequest.getFirstname(),
                "lastname", signupRequest.getLastname(),
                "dateOfBirth", signupRequest.getDateOfBirth().toString(),
                "gender", signupRequest.getGender()
        )));
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void logoutUser(final HttpServletRequest request, final HttpServletResponse response) {
        String accessToken = getCookieValue(request, "accessToken");
        checkNotNullAccessDeniedException(accessToken,
                "Access token cannot be found.");
        tokenService.removeAccessTokenFromCookie(response);
        tokenService.removeRefreshTokenFromCookieAndExpire(response, accessToken);
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
            tokenService.generateToken(email);
            return email;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid refresh token.");
        }
    }

    public void addUserDetailsInHeader(
            final HttpServletResponse response) {
        AuthUser userDetails = getCurrentUserDetails();
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

    public static AuthUser getCurrentUserDetails() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
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
            throw new ValidationException(message);
        }
    }
}
