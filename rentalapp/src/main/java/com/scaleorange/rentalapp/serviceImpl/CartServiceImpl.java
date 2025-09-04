package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.CartRequestDTO;
import com.scaleorange.rentalapp.dtos.CartResponseDTO;
import com.scaleorange.rentalapp.entitys.Cart;
import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.repository.CartRepository;
import com.scaleorange.rentalapp.repository.LaptopRepository;
import com.scaleorange.rentalapp.repository.UsersRepository;
import com.scaleorange.rentalapp.service.CartService;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final LaptopRepository laptopRepository;
    private final RentalService rentalService;

    private String generateUid() {
        return java.util.UUID.randomUUID().toString().substring(0, 12);
    }

    private String getCurrentUserUid() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public CartResponseDTO addToCart(CartRequestDTO request) {
        String userUid = getCurrentUserUid();
        Users user = usersRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserUid(user.getUid())
                .orElse(Cart.builder().uid(generateUid()).user(user).laptops(new ArrayList<>()).build());

        for (String laptopUid : request.getLaptopUids()) {
            Laptops laptop = laptopRepository.findByUid(laptopUid)
                    .orElseThrow(() -> new RuntimeException("Laptop not found: " + laptopUid));

            if (!cart.getLaptops().contains(laptop)) {
                cart.getLaptops().add(laptop);
            }
        }

        Cart saved = cartRepository.save(cart);
        return toDTO(saved);
    }

    @Override
    public CartResponseDTO removeFromCart(CartRequestDTO request) {
        String userUid = getCurrentUserUid();
        Cart cart = cartRepository.findByUserUid(userUid)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getLaptops().removeIf(laptop -> request.getLaptopUids().contains(laptop.getUid()));
        Cart saved = cartRepository.save(cart);
        return toDTO(saved);
    }

    @Override
    public CartResponseDTO getCart() {
        String userUid = getCurrentUserUid();
        Cart cart = cartRepository.findByUserUid(userUid)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return toDTO(cart);
    }

    @Override
    public CartResponseDTO checkout() {
        String userUid = getCurrentUserUid();
        Cart cart = cartRepository.findByUserUid(userUid)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getLaptops().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        rentalService.createRentalForCart(userUid, cart.getLaptops());

        cart.getLaptops().clear(); // optional: empty cart after checkout
        cartRepository.save(cart);

        return toDTO(cart);
    }

    private CartResponseDTO toDTO(Cart cart) {
        return CartResponseDTO.builder()
                .cartUid(cart.getUid())
                .userUid(cart.getUser().getUid())
                .laptopUids(cart.getLaptops().stream().map(Laptops::getUid).collect(Collectors.toList()))
                .build();
    }
}
