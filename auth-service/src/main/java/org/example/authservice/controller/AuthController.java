package org.example.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.LoginResponse;
import org.example.authservice.dto.SignupRequest;
import org.example.authservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authenticationService;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody final LoginRequest loginRequest,
            final HttpServletResponse response) {
        LoginResponse tokens = authenticationService.login(loginRequest);
        LOGGER.info("User {} logged in", loginRequest.getEmail());
        response.setHeader("Authorization", "Bearer " + tokens.getAccessToken());
        response.setHeader("X-Refresh-Token", tokens.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody final SignupRequest signupRequest) {
        authenticationService.registerAccount(signupRequest);
        LOGGER.info("User {} registered", signupRequest.getEmail());
        return new ResponseEntity<>("Account registered.",
                HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(final HttpServletRequest request, final HttpServletResponse response) {
        authenticationService.logoutUser(request, response);
        return new ResponseEntity<>("You've been signed out!", HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            final HttpServletRequest request,
            final HttpServletResponse response) {
        String email = authenticationService.refreshToken(
                request, response);
        LOGGER.info("User {} refreshed token", email);
        return new ResponseEntity<>(
                "User " + email + " refreshed tokens successfully",
                HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<?> addUserDetailsInHeader(final HttpServletResponse response) {
        LOGGER.info("Fetching current user details");
        authenticationService.addUserDetailsInHeader(response);
        LOGGER.info("Fetching current user details and adding to header");
        return new ResponseEntity<>(
                "User details added to header",
                HttpStatus.OK);
    }
}
