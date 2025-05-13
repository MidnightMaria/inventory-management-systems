package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.AuthRequest;
import com.agnesmaria.inventory.springboot.dto.AuthResponse;
import com.agnesmaria.inventory.springboot.dto.RegisterRequest;
import com.agnesmaria.inventory.springboot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Authentication", description = "APIs for user authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "User registration", description = "Register new user")
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}