package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.LaptopTransactionDTO;
import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.dtos.ReturnRequestDTO;
import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.entitys.RentalOrder;

import java.time.LocalDateTime;
import java.util.List;

public interface RentalService {

    RentalResponseDTO createRental(RentalRequestDTO request);

    RentalResponseDTO createRentalForCart(List<Laptops> laptops, long numberOfMonths,
                                          LocalDateTime rentalTime, LocalDateTime returnTime);

    // Mark rental as paid
    void markRentalAsPaid(RentalOrder rental);

    // Handle failed rental
    void handleFailedRental(RentalOrder rental);

    /**
     * Handle the return of a laptop by the consumer.
     * Calculates late fee, updates transaction and laptop status,
     * handles deposit release if approved, and updates RentalOrder if all laptops are returned.
     *
     * @param request               LaptopRentalTransaction being returned
     * @param txnUid True if vendor approves release of security deposit
     */
    LaptopTransactionDTO returnLaptop(String txnUid, ReturnRequestDTO request);

    /**
     * Fetch a laptop rental transaction by its UID.
     * Used when processing returns, refunds, etc.
     *
     * @param uid transaction unique identifier
     * @return LaptopRentalTransaction if found
     */
    LaptopRentalTransaction getTransactionByUid(String uid);

}
