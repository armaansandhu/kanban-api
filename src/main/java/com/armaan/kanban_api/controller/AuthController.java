package com.armaan.kanban_api.controller;

import com.armaan.kanban_api.dto.response.ApiResponse;
import com.armaan.kanban_api.dto.response.AuthResponse;
import com.armaan.kanban_api.dto.request.LoginRequest;
import com.armaan.kanban_api.dto.request.RegisterRequest;
import com.armaan.kanban_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());

        AuthResponse response = authService.register(request);
        log.info("User registered successfully: {}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());

        AuthResponse response = authService.login(request);
        log.info("User logged in successfully: {}", response.getUsername());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        log.info("Logout request received");
        // Placeholder: JWT is stateless, and it must be handled at client side.
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }
}