package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.AuthRequest;
import com.agnesmaria.inventory.springboot.dto.AuthResponse;
import com.agnesmaria.inventory.springboot.dto.RegisterRequest;
import com.agnesmaria.inventory.springboot.model.Role;
import com.agnesmaria.inventory.springboot.model.User;
import com.agnesmaria.inventory.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(), // <--- GUNAKAN USERNAME DARI REQUEST
                request.getPassword()
            )
        );

        User user = userRepository.findByUsername(request.getUsername()) // <--- CARI BERDASARKAN USERNAME
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + request.getUsername()));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
            .token(jwtToken)
            .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Role userRole = Role.USER; // Default role

        // Logic sementara untuk memberikan peran ADMIN atau WAREHOUSE_MANAGER berdasarkan username
        if (request.getUsername().equalsIgnoreCase("admin")) {
            userRole = Role.ADMIN;
        } else if (request.getUsername().equalsIgnoreCase("warehouse")) {
            userRole = Role.WAREHOUSE_MANAGER;
        }

        User newUser = User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(userRole)
            .build();

        userRepository.save(newUser);

        String jwtToken = jwtService.generateToken(newUser);

        return AuthResponse.builder()
            .token(jwtToken)
            .build();
    }
}