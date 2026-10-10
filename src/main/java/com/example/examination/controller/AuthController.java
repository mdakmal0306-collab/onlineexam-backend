package com.example.examination.controller;

import com.example.examination.dto.ApiResponse;
import com.example.examination.dto.AuthRequest;
import com.example.examination.dto.AuthResponse;
import com.example.examination.dto.RegisterRequest;
import com.example.examination.dto.UserDTO;
import com.example.examination.security.UserPrincipal;
import com.example.examination.service.AuthService;
import com.example.examination.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerStudent(registerRequest);
        return ResponseEntity.ok(ApiResponse.ok("Student registration successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        UserDTO userDTO = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved", userDTO));
    }
}
