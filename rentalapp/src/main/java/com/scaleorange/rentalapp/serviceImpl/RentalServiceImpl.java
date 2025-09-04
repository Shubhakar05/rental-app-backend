package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.entitys.RentalOrder;
import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import com.scaleorange.rentalapp.repository.LaptopRepository;
import com.scaleorange.rentalapp.repository.RentalOrderRepository;
import com.scaleorange.rentalapp.repository.UsersRepository;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalOrderRepository rentalRepository;
    private final LaptopRepository laptopRepository;
    private final UsersRepository usersRepository;

    // Utility method to get authenticated user from JWT
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

    // Utility method to lock laptops and calculate total amount
    private BigDecimal lockLaptopsAndCalculateAmount(List<Laptops> laptops, long numberOfMonths) {
        laptops.forEach(laptop -> {
            if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                throw new RuntimeException("Laptop not available: " + laptop.getUid());
            }
            laptop.setStatus(LaptopStatusEnum.LOCKED);
            laptop.setLockTime(LocalDateTime.now());
            laptopRepository.save(laptop);
        });

        return laptops.stream()
                .map(laptop -> BigDecimal.valueOf(laptop.getPricePerMonth()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(numberOfMonths));
    }

    @Override
    public RentalResponseDTO createRental(RentalRequestDTO request) {
        Users user = getAuthenticatedUser();

        List<Laptops> laptops = laptopRepository.findAllByUidIn(request.getLaptopUids());
        if (laptops.isEmpty()) {
            throw new RuntimeException("No laptops found for the given IDs");
        }

        LocalDateTime rentalTime = request.getRentalTime() != null ? request.getRentalTime() : LocalDateTime.now();
        LocalDateTime returnTime = rentalTime.plusMonths(request.getNumberOfMonths());

        BigDecimal totalAmount = lockLaptopsAndCalculateAmount(laptops, request.getNumberOfMonths());

        RentalOrder rental = RentalOrder.builder()
                .user(user)
                .laptops(laptops)
                .status(RentalStatusEnum.PENDING)
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(request.getNumberOfMonths())
                .totalAmount(totalAmount)
                .build();

        rentalRepository.save(rental);

        return RentalResponseDTO.builder()
                .rentalUid(rental.getUid())
                .userUid(user.getUid())
                .laptopUids(laptops.stream().map(Laptops::getUid).toList())
                .brand(request.getBrand())
                .status(rental.getStatus())
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(request.getNumberOfMonths())
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    public void createRentalForCart(List<Laptops> laptops, long numberOfMonths,
                                    LocalDateTime rentalTime, LocalDateTime returnTime, BigDecimal totalAmount) {
        Users user = getAuthenticatedUser();

        if (laptops.isEmpty()) {
            throw new RuntimeException("No laptops provided in the cart");
        }

        // Default rental and return times if null
        if (rentalTime == null) rentalTime = LocalDateTime.now();
        if (returnTime == null) returnTime = rentalTime.plusMonths(numberOfMonths);

        // Lock laptops and calculate total amount if not provided
        if (totalAmount == null) {
            totalAmount = lockLaptopsAndCalculateAmount(laptops, numberOfMonths);
        } else {
            // Still lock laptops to prevent double booking
            laptops.forEach(laptop -> {
                if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                    throw new RuntimeException("Laptop not available: " + laptop.getUid());
                }
                laptop.setStatus(LaptopStatusEnum.LOCKED);
                laptop.setLockTime(LocalDateTime.now());
                laptopRepository.save(laptop);
            });
        }

        RentalOrder rental = RentalOrder.builder()
                .user(user)
                .laptops(laptops)
                .status(RentalStatusEnum.PENDING)
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(numberOfMonths)
                .totalAmount(totalAmount)
                .build();

        rentalRepository.save(rental);
    }
}
