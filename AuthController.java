package com.sih.demo.controller;

import com.sih.demo.dto.AuthDtos.*;
import com.sih.demo.entity.mysql.User;
import com.sih.demo.repository.mysql.UserRepository;
import com.sih.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.getUsername() == null || req.getUsername().isBlank())
            return ResponseEntity.badRequest().body("Username is required");
        if (req.getPassword() == null || req.getPassword().length() < 6)
            return ResponseEntity.badRequest().body("Password must contain at least 6 characters");
        if (userRepository.existsByUsername(req.getUsername().trim()))
            return ResponseEntity.badRequest().body("Username already exists");

        User user = User.builder()
                .username(req.getUsername().trim())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .badgeNumber(req.getBadgeNumber())
                .role(User.Role.INVESTIGATOR)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok("Investigator registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.getUsername());
        String token = jwtService.generateToken(userDetails);
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build());
    }
}
