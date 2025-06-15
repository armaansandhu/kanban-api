package com.armaan.kanban_api.controller;

import com.armaan.kanban_api.dto.response.UserProfileResponse;
import com.armaan.kanban_api.entity.User;
import com.armaan.kanban_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class KanbanController {

    private final AuthService authService;

    @GetMapping("/user/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Profile request for user: {}", user.getUsername());

        UserProfileResponse profile = authService.getUserProfile(user.getUsername());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/user/dashboard")
    public ResponseEntity<String> dashboard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Dashboard access for user: {}", user.getUsername());

        return ResponseEntity.ok("Welcome to dashboard, " + user.getUsername() + "!");
    }
}
