package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.repository.UsersRepository;
import com.scaleorange.rentalapp.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * Login endpoint: validates credentials and generates JWT token with multi-role support.
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
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

            // Wrap role in a list (supports multi-role users in the future)
            List<String> roles = List.of(user.getRole().name());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), roles);

            return Map.of(
                    "message", "Login successful",
                    "token", token,
                    "roles", roles,
                    "email", user.getEmail(),
                    "uid", user.getUid()
            );

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
