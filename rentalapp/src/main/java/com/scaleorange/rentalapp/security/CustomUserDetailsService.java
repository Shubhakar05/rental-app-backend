package com.scaleorange.rentalapp.security;

import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.repository.UsersRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = user.getRole() != null ? user.getRole().name().toUpperCase() : "USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // BCrypt encoded
                .authorities("ROLE_" + role) // ensures Spring matches JWT authorities
                .accountLocked(!user.isVerified()) // optional: lock if not verified
                .build();
    }
}
