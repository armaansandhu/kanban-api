package com.armaan.kanban_api.service;

import com.armaan.kanban_api.dto.response.AuthResponse;
import com.armaan.kanban_api.dto.request.LoginRequest;
import com.armaan.kanban_api.dto.request.RegisterRequest;
import com.armaan.kanban_api.dto.response.UserProfileResponse;
import com.armaan.kanban_api.entity.User;
import com.armaan.kanban_api.exception.InvalidCredentialsException;
import com.armaan.kanban_api.exception.UserAlreadyExistsException;
import com.armaan.kanban_api.repository.UserRepository;
import com.armaan.kanban_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with username: {}", request.getUsername());

        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("Registration failed - Username already exists: {}", request.getUsername());
                throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' is already taken!");
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Registration failed - Email already exists: {}", request.getEmail());
                throw new UserAlreadyExistsException("Email '" + request.getEmail() + "' is already in use!");
            }

            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .avatarUrl(request.getAvatarUrl())
                    .isActive(true)
                    .emailVerified(false)
                    .build();

            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(savedUser);

            log.info("User registered successfully with ID: {} and username: {}",
                    savedUser.getId(), savedUser.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .avatarUrl(savedUser.getAvatarUrl())
                    .emailVerified(savedUser.getEmailVerified())
                    .createdAt(savedUser.getCreatedAt())
                    .build();

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration for username: {}",
                    request.getUsername(), e);
            throw new RuntimeException("Registration failed. Please try again later.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to authenticate user: {}", request.getUsernameOrEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Update last login time
            userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());

            String token = jwtUtil.generateToken(user);

            log.info("User authenticated successfully: {} (ID: {})",
                    user.getUsername(), user.getId());

            return AuthResponse.builder()
                    .token(token)
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .avatarUrl(user.getAvatarUrl())
                    .emailVerified(user.getEmailVerified())
                    .createdAt(user.getCreatedAt())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {} - Invalid credentials",
                    request.getUsernameOrEmail());
            throw new InvalidCredentialsException("Invalid username/email or password");
        } catch (DisabledException e) {
            log.warn("Authentication failed for user: {} - Account disabled",
                    request.getUsernameOrEmail());
            throw e;
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}",
                    request.getUsernameOrEmail(), e.getMessage());
            throw new InvalidCredentialsException("Authentication failed");
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user: {}",
                    request.getUsernameOrEmail(), e);
            throw new RuntimeException("Login failed. Please try again later.");
        }
    }

    public UserProfileResponse getUserProfile(String username) {
        log.debug("Fetching profile for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User profile not found: {}", username);
                    return new RuntimeException("User not found");
                });

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}