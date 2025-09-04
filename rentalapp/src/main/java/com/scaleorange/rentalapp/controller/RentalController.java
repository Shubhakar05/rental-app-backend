package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<RentalResponseDTO> createRental(@RequestBody RentalRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userUid = auth.getName(); // UID from JWT

        return ResponseEntity.ok(rentalService.createRental(request, userUid));
    }
}
