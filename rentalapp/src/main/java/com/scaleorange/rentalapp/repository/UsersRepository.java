package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import com.scaleorange.rentalapp.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByRole(RoleEnum role);

    Optional<Users> findByUid(String uid);
    Optional<Users> findByEmail(String email);
    List<Users> findByVerificationStatus(VerificationStatusEnum status);


}
