package com.backend.notes.auth;

import com.backend.notes.AbstractIntegrationTest;
import com.backend.notes.auth.dto.AuthResponse;
import com.backend.notes.auth.dto.LoginRequest;
import com.backend.notes.auth.dto.RegisterRequest;
import com.backend.notes.auth.dto.RefreshTokenRequest;
import com.backend.notes.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends AbstractIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testRegister_Success() {
        RegisterRequest request = RegisterRequest.builder()
            .email("newuser@example.com")
            .password("SecurePassword123")
            .build();
        
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/register",
            request,
            AuthResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(response.getBody().getRefreshToken()).isNotNull();
        assertThat(response.getBody().getExpiresIn()).isEqualTo(900);
        
        // Verify user was created
        assertThat(userRepository.findByEmailIgnoreCase("newuser@example.com")).isPresent();
    }
    
    @Test
    void testRegister_InvalidEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .email("invalid-email")
            .password("SecurePassword123")
            .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/register",
            request,
            String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    void testRegister_WeakPassword() {
        RegisterRequest request = RegisterRequest.builder()
            .email("user@example.com")
            .password("short")
            .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/register",
            request,
            String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    void testRegister_DuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .email("duplicate@example.com")
            .password("SecurePassword123")
            .build();
        
        // Register first time
        ResponseEntity<AuthResponse> response1 = restTemplate.postForEntity(
            "/register",
            request,
            AuthResponse.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Register same email second time
        ResponseEntity<String> response2 = restTemplate.postForEntity(
            "/register",
            request,
            String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
    
    @Test
    void testLogin_Success() {
        // Register first
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("login@example.com")
            .password("SecurePassword123")
            .build();
        
        restTemplate.postForEntity("/register", registerRequest, AuthResponse.class);
        
        // Login
        LoginRequest loginRequest = LoginRequest.builder()
            .email("login@example.com")
            .password("SecurePassword123")
            .build();
        
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/login",
            loginRequest,
            AuthResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(response.getBody().getExpiresIn()).isEqualTo(900);
    }
    
    @Test
    void testLogin_InvalidPassword() {
        // Register first
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("wrongpwd@example.com")
            .password("SecurePassword123")
            .build();
        
        restTemplate.postForEntity("/register", registerRequest, AuthResponse.class);
        
        // Login with wrong password
        LoginRequest loginRequest = LoginRequest.builder()
            .email("wrongpwd@example.com")
            .password("WrongPassword123")
            .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/login",
            loginRequest,
            String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    void testLogin_UnknownUser() {
        LoginRequest loginRequest = LoginRequest.builder()
            .email("nonexistent@example.com")
            .password("SecurePassword123")
            .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/login",
            loginRequest,
            String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    void testRefreshToken_Success() {
        // Register and get refresh token
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("refresh@example.com")
            .password("SecurePassword123")
            .build();
        
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
            "/register",
            registerRequest,
            AuthResponse.class);
        
        AuthResponse authResponse = registerResponse.getBody();
        assertThat(authResponse).isNotNull();
        
        // Refresh token
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
            .refreshToken(authResponse.getRefreshToken())
            .build();
        
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/auth/refresh",
            refreshRequest,
            AuthResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
    }
    
    @Test
    void testRefreshToken_InvalidToken() {
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
            .refreshToken("invalid-token-123")
            .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/auth/refresh",
            refreshRequest,
            String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
