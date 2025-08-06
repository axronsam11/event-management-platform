package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.auth.AuthRequest;
import com.eventmanagement.api.dto.auth.AuthResponse;
import com.eventmanagement.api.dto.auth.RegisterRequest;
import com.eventmanagement.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling authentication requests.
 * Provides endpoints for user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * @param registerRequest The registration request containing user details
     * @return ResponseEntity with the authentication response containing JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(registerRequest));
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param authRequest The authentication request containing email and password
     * @return ResponseEntity with the authentication response containing JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }
}