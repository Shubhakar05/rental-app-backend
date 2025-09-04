package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.repository.UsersRepository;
import com.scaleorange.rentalapp.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UsersRepository usersRepository) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.usersRepository = usersRepository;
    }

    /**
     * Login endpoint: validates credentials and generates JWT token.
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        try {
            // Authenticate credentials
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Fetch the user from DB
            Users user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Determine role (default USER if null)
            String role = user.getRole() != null ? user.getRole().name() : "USER";

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), role);

            return Map.of(
                    "message", "Login successful",
                    "token", token,
                    "role", role,
                    "email", user.getEmail(),
                    "uid", user.getUid()
            );

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
