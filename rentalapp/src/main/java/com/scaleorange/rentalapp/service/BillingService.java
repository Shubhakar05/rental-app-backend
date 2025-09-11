package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.LaptopTransactionDTO;
import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import com.scaleorange.rentalapp.entitys.Laptops;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillingService {

    /**
     * Calculate billing for a list of laptops for the given rental period.
     * Returns a DTO containing all calculated financial details.
     *
     * @param laptops List of laptops being rented
     * @param startDate Rental start date
     * @param endDate Rental end date
     * @param rentalOrderUid UID of the rental order
     * @param vendorUid UID of the vendor
     * @param consumerUid UID of the consumer
     * @return LaptopTransactionDTO with billing breakdown (base, GST, deposit, late fee, total)
     */
    LaptopTransactionDTO calculateBillingDTO(List<Laptops> laptops,
                                             LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             String rentalOrderUid,
                                             String vendorUid,
                                             String consumerUid);

    /**
     * Calculate late fee for a given LaptopRentalTransaction.
     * Can be used after laptop return or in batch processing.
     */
    BigDecimal calculateLateFee(LaptopRentalTransaction txn);

    /**
     * Update transaction's late fee and totalAmount.
     * Should be called after laptop return or in batch processing.
     */
    void updateLateFee(LaptopRentalTransaction txn);
}
