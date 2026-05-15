package com.backend.notes.auth;

import com.backend.notes.auth.dto.AuthResponse;
import com.backend.notes.auth.dto.LoginRequest;
import com.backend.notes.auth.dto.RefreshTokenRequest;
import com.backend.notes.auth.dto.RegisterRequest;
import com.backend.notes.user.User;
import com.backend.notes.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and token management")
public class AuthController {
    
    private final UserService userService;
    private final AuthService authService;
    
    @PostMapping("register")
    @Operation(summary = "Register a new user", description = "Create a new user account with email and password")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.getEmail(), request.getPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Email already registered");
        }
    }
    
    @PostMapping("login")
    @Operation(summary = "Login", description = "Authenticate with email and password to receive access and refresh tokens")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("auth/refresh")
    @Operation(summary = "Refresh access token", description = "Use refresh token to get a new access token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("auth/logout")
    @Operation(summary = "Logout", description = "Revoke the refresh token")
    @ApiResponse(responseCode = "204", description = "Logout successful")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
