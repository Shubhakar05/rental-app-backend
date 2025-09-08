package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRentalOrderId(Long rentalOrderId);
}
