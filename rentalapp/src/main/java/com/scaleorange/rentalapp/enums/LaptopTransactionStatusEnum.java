package com.scaleorange.rentalapp.enums;

public enum LaptopTransactionStatusEnum {
    PENDING,   // Transaction created, waiting for payment
    RENTED,    // Payment success → Laptop allocated
    RETURNED,  // Laptop returned on time
    OVERDUE,   // Rental period ended but not yet returned
    REPLACED,  // Laptop swapped with another
    FAILED     // Payment failed or transaction aborted
}
