package com.scaleorange.rentalapp.enums;

public enum RentalStatusEnum {
    PENDING,     // Order created, waiting for payment
    PAID,        // Payment success
    ACTIVE,      // Rental started, laptops handed over
    COMPLETED,   // Rental ended, laptops returned
    FAILED,      // Payment failed
    CANCELLED    // Order cancelled before start
}
