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


    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestBody CartRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }


    @PostMapping("/remove")
    public ResponseEntity<CartResponseDTO> removeFromCart(@RequestBody CartRequestDTO request) {
        return ResponseEntity.ok(cartService.removeFromCart(request));
    }


    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }


    @PostMapping("/checkout")
    public ResponseEntity<CartResponseDTO> checkout(@RequestParam long numberOfMonths) {
        return ResponseEntity.ok(cartService.checkout(numberOfMonths));
    }
}
