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
     * Uses authenticated user from JWT context.
     *
     * @param request DTO containing laptop IDs, number of months, and optional rental time.
     * @return RentalResponseDTO with rental details including total amount.
     */
    RentalResponseDTO createRental(RentalRequestDTO request);

    /**
     * Create a rental order from cart items for multiple laptops.
     * Uses authenticated user from JWT context.
     *
     * @param laptops        List of laptops in the cart
     * @param numberOfMonths Number of months for the rental
     * @param rentalTime     Start time of rental
     * @param returnTime     Expected return time
     * @param totalAmount    Total rental amount
     */
    void createRentalForCart(List<Laptops> laptops, long numberOfMonths,
                             LocalDateTime rentalTime, LocalDateTime returnTime, BigDecimal totalAmount);

}
