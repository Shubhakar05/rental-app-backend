package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.CartRequestDTO;
import com.scaleorange.rentalapp.dtos.CartResponseDTO;
import com.scaleorange.rentalapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestBody CartRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/remove")
    public ResponseEntity<CartResponseDTO> removeFromCart(@RequestBody CartRequestDTO request) {
        return ResponseEntity.ok(cartService.removeFromCart(request));
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/checkout")
    public ResponseEntity<CartResponseDTO> checkout() {
        return ResponseEntity.ok(cartService.checkout());
    }
}
