package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.entitys.Laptops;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public interface RentalService {

    /**
     * Create a rental order by selecting laptops for a given number of months.
     *
     * @param request   DTO containing laptop IDs, number of months, and optional rental time.
     * @param userUid   UID of the logged-in user (from JWT)
     * @return          RentalResponseDTO with rental details including total amount.
     */
    RentalResponseDTO createRental(RentalRequestDTO request, String userUid);

    /**
     * Create a rental order from cart items for multiple laptops.
     *
     * @param userUid        UID of the logged-in user (from JWT)
     * @param laptops        List of laptops in the cart
     * @param numberOfMonths Number of months for the rental
     * @param rentalTime     Start time of rental
     * @param returnTime     Expected return time
     * @param totalAmount    Total rental amount
     */
    void createRentalForCart(String userUid, List<Laptops> laptops, long numberOfMonths,
                             LocalDateTime rentalTime, LocalDateTime returnTime, BigDecimal totalAmount);

}
