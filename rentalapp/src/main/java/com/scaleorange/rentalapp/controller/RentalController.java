package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * Create rental
     * Allowed Roles: COMPANY_ADMIN, SUPER_ADMIN
     */
    @PostMapping("/create")
    public ResponseEntity<RentalResponseDTO> createRental(@RequestBody RentalRequestDTO request) {
        // No need to fetch userUid here; service will get it from JWT
        RentalResponseDTO response = rentalService.createRental(request);
        return ResponseEntity.ok(response);
    }
}
