package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.RentalOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    Optional<RentalOrder> findByUid(String uid);


}
