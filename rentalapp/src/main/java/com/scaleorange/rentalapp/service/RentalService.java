package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.entitys.Laptops;

import java.util.List;

public interface RentalService {

    /**
     * Create a rental order by selecting laptops based on brand and price.
     *
     * @param request DTO containing brand, price per month, and rental period.
     * @param userUid UID of the logged-in user (from JWT)
     * @return RentalResponseDTO with rental details.
     */
    RentalResponseDTO createRental(RentalRequestDTO request, String userUid);

    void createRentalForCart(String userUid, List<Laptops> laptops);
}
