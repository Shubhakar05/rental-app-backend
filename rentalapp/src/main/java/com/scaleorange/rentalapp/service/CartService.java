package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.CartRequestDTO;
import com.scaleorange.rentalapp.dtos.CartResponseDTO;

public interface CartService {

    /**
     * Add laptops to the current user's cart (UID from JWT).
     */
    CartResponseDTO addToCart(CartRequestDTO request);

    /**
     * Remove laptops from the current user's cart (UID from JWT).
     */
    CartResponseDTO removeFromCart(CartRequestDTO request);

    /**
     * Get the current user's cart (UID from JWT).
     */
    CartResponseDTO getCart();

    /**
     * Checkout the current user's cart and create a rental (UID from JWT).
     */
    CartResponseDTO checkout();
}
