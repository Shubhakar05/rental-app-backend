package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    // Find user by email (used in login)
    Optional<Users> findByEmail(String email);

    // Find user by UID (short unique string)
    Optional<Users> findByUid(String uid);

    // Check if username already exists
    boolean existsByUsername(String username);

    // Check if email already exists
    boolean existsByEmail(String email);

    // Find Super Admin by role (unique)
    Optional<Users> findByRole(RoleEnum role);  // UPDATED

    // **Check if any user exists with the given role**
    boolean existsByRole(RoleEnum role);

    // Find users by verification status (pending, approved, rejected)
    List<Users> findByVerificationStatus(VerificationStatusEnum status);
}
