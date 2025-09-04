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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private Users getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @Override
    public CartResponseDTO addToCart(CartRequestDTO request) {
        Users user = getAuthenticatedUser();

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
        return toDTO(saved, null, null, null, null);
    }

    @Override
    public CartResponseDTO removeFromCart(CartRequestDTO request) {
        Users user = getAuthenticatedUser();

        Cart cart = cartRepository.findByUserUid(user.getUid())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getLaptops().removeIf(laptop -> request.getLaptopUids().contains(laptop.getUid()));
        Cart saved = cartRepository.save(cart);
        return toDTO(saved, null, null, null, null);
    }

    @Override
    public CartResponseDTO getCart() {
        Users user = getAuthenticatedUser();

        Cart cart = cartRepository.findByUserUid(user.getUid())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return toDTO(cart, null, null, null, null);
    }

    @Override
    public CartResponseDTO checkout(long numberOfMonths) {
        Users user = getAuthenticatedUser();

        Cart cart = cartRepository.findByUserUid(user.getUid())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getLaptops().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total amount
        BigDecimal totalAmount = cart.getLaptops().stream()
                .map(laptop -> BigDecimal.valueOf(laptop.getPricePerMonth())
                        .multiply(BigDecimal.valueOf(numberOfMonths)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime rentalTime = LocalDateTime.now();
        LocalDateTime returnTime = rentalTime.plusMonths(numberOfMonths);

        // Call RentalService to create rental for cart
        rentalService.createRentalForCart(cart.getLaptops(), numberOfMonths, rentalTime, returnTime, totalAmount);

        // Clear cart after checkout
        cart.getLaptops().clear();
        cartRepository.save(cart);

        return toDTO(cart, numberOfMonths, totalAmount, rentalTime, returnTime);
    }

    private CartResponseDTO toDTO(Cart cart, Long numberOfMonths, BigDecimal totalAmount,
                                  LocalDateTime rentalTime, LocalDateTime returnTime) {
        return CartResponseDTO.builder()
                .cartUid(cart.getUid())
                .userUid(cart.getUser().getUid())
                .laptopUids(cart.getLaptops().stream().map(Laptops::getUid).collect(Collectors.toList()))
                .numberOfMonths(numberOfMonths)
                .totalAmount(totalAmount)
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .build();
    }
}
