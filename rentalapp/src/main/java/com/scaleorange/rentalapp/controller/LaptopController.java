package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.LaptopRequestDTO;
import com.scaleorange.rentalapp.dtos.LaptopResponseDTO;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import com.scaleorange.rentalapp.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/laptops")
@RequiredArgsConstructor
public class LaptopController {

    private final LaptopService laptopService;

    // Vendor or Super Admin can add laptops

    @PostMapping
    public ResponseEntity<LaptopResponseDTO> addLaptop(@ModelAttribute LaptopRequestDTO request) {
        String vendorUid = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(laptopService.addLaptop(request, vendorUid));
    }


    @PutMapping("/{uid}")
    public ResponseEntity<LaptopResponseDTO> updateLaptop(@PathVariable String uid,
                                                          @RequestBody LaptopRequestDTO request) {
        String vendorUid = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(laptopService.updateLaptop(uid, request, vendorUid));
    }


    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteLaptop(@PathVariable String uid) {
        String vendorUid = SecurityContextHolder.getContext().getAuthentication().getName();
        laptopService.deleteLaptop(uid, vendorUid);
        return ResponseEntity.noContent().build();
    }

    // Anyone with auth can read

    @GetMapping("/{uid}")
    public ResponseEntity<LaptopResponseDTO> getLaptop(@PathVariable String uid) {
        return ResponseEntity.ok(laptopService.getLaptop(uid));
    }


    @GetMapping
    public ResponseEntity<List<LaptopResponseDTO>> getAllLaptops() {
        return ResponseEntity.ok(laptopService.getAllLaptops());
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<LaptopResponseDTO>> getLaptopsByStatus(@PathVariable LaptopStatusEnum status) {
        return ResponseEntity.ok(laptopService.getLaptopsByStatus(status));
    }
}
