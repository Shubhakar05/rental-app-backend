package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.LaptopTransactionDTO;
import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.dtos.ReturnRequestDTO;
import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // ------------------- 1️⃣ Single-laptop rental -------------------
    @PostMapping("/create")
    public ResponseEntity<RentalResponseDTO> createRental(@RequestBody RentalRequestDTO request) {
        RentalResponseDTO response = rentalService.createRental(request);
        return ResponseEntity.ok(response);
    }


    // ------------------- 3️⃣ Laptop return with deposit handling -------------------
    @PostMapping("/return/{txnUid}")
    public ResponseEntity<LaptopTransactionDTO> returnLaptop(
            @PathVariable String txnUid,
            @RequestBody ReturnRequestDTO request) {

        LaptopTransactionDTO response = rentalService.returnLaptop(txnUid, request);

        return ResponseEntity.ok(response);
    }
}
