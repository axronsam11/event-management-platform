package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.auth.AuthRequest;
import com.eventmanagement.api.dto.auth.AuthResponse;
import com.eventmanagement.api.dto.auth.RegisterRequest;
import com.eventmanagement.api.exception.ResourceAlreadyExistsException;
import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.UserRepository;
import com.eventmanagement.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for handling authentication operations.
 * Manages user registration and login.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Register a new user.
     *
     * @param request The registration request
     * @return AuthResponse containing JWT token and user details
     * @throws ResourceAlreadyExistsException if a user with the same email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // Set default role if none provided
        List<String> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = Collections.singletonList("ATTENDEE");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Return auth response with token
        return AuthResponse.withBearerToken(
                jwt,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRoles()
        );
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param request The authentication request
     * @return AuthResponse containing JWT token and user details
     */
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        return AuthResponse.withBearerToken(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles()
        );
    }
}