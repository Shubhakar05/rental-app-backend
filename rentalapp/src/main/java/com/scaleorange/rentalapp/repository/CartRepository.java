package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUid(String uid);
    Optional<Cart> findByUserUid(String userUid);
}
