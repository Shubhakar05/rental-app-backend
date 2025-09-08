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

    // Global GST rate for rentals
    private static final BigDecimal GST_RATE = BigDecimal.valueOf(18); // 18%

    // Helper class for amount breakdown
    private static class AmountBreakdown {
        BigDecimal baseAmount;
        BigDecimal totalGst;
        BigDecimal totalAmount;

        AmountBreakdown(BigDecimal baseAmount, BigDecimal totalGst, BigDecimal totalAmount) {
            this.baseAmount = baseAmount;
            this.totalGst = totalGst;
            this.totalAmount = totalAmount;
        }
    }

    // Get authenticated user from JWT
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
    public RentalResponseDTO createRental(RentalRequestDTO request) {
        Users user = getAuthenticatedUser();

        // Fetch single laptop from request
        String laptopUid = request.getLaptopUids().get(0);
        Laptops laptop = laptopRepository.findByUid(laptopUid)
                .orElseThrow(() -> new RuntimeException("Laptop not found: " + laptopUid));

        if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
            throw new RuntimeException("Laptop not available: " + laptopUid);
        }

        List<Laptops> laptops = List.of(laptop);

        LocalDateTime rentalTime = request.getRentalTime() != null ? request.getRentalTime() : LocalDateTime.now();
        LocalDateTime returnTime = rentalTime.plusMonths(request.getNumberOfMonths());

        // Calculate breakdown
        AmountBreakdown breakdown = calculateAmounts(laptops, request.getNumberOfMonths());

        RentalOrder rental = RentalOrder.builder()
                .user(user)
                .laptops(laptops)
                .status(RentalStatusEnum.PENDING)
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(request.getNumberOfMonths())
                .baseAmount(breakdown.baseAmount)   // add this
                .totalGst(breakdown.totalGst)       // add this
                .totalAmount(breakdown.totalAmount)
                .build();

        rentalRepository.save(rental);

        return RentalResponseDTO.builder()
                .rentalUid(rental.getUid())
                .userUid(user.getUid())
                .laptopUids(List.of(laptop.getUid()))
                .brand(laptop.getBrand())
                .status(rental.getStatus())
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(request.getNumberOfMonths())
                .baseAmount(breakdown.baseAmount)
                .totalGst(breakdown.totalGst)
                .totalAmount(breakdown.totalAmount)
                .build();
    }

    // Calculate base, GST, and total
    private AmountBreakdown calculateAmounts(List<Laptops> laptops, long numberOfMonths) {
        laptops.forEach(laptop -> {
            if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                throw new RuntimeException("Laptop not available: " + laptop.getUid());
            }
        });

        // Base total
        BigDecimal baseTotal = laptops.stream()
                .map(laptop -> BigDecimal.valueOf(laptop.getPricePerMonth()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(numberOfMonths));

        // GST total
        BigDecimal totalGst = baseTotal.multiply(GST_RATE).divide(BigDecimal.valueOf(100));

        // Final amount
        BigDecimal totalAmount = baseTotal.add(totalGst);

        return new AmountBreakdown(baseTotal, totalGst, totalAmount);
    }

    @Override
    public void createRentalForCart(List<Laptops> laptops, long numberOfMonths,
                                    LocalDateTime rentalTime, LocalDateTime returnTime, BigDecimal totalAmount) {
        Users user = getAuthenticatedUser();

        if (laptops.isEmpty()) {
            throw new RuntimeException("No laptops provided in the cart");
        }

        if (rentalTime == null) rentalTime = LocalDateTime.now();
        if (returnTime == null) returnTime = rentalTime.plusMonths(numberOfMonths);

        AmountBreakdown breakdown;
        if (totalAmount == null) {
            breakdown = calculateAmounts(laptops, numberOfMonths);
            totalAmount = breakdown.totalAmount;
        } else {
            laptops.forEach(laptop -> {
                if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                    throw new RuntimeException("Laptop not available: " + laptop.getUid());
                }
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

    public void markRentalAsPaid(RentalOrder rental) {
        rental.setStatus(RentalStatusEnum.ACTIVE);
        rentalRepository.save(rental);

        rental.getLaptops().forEach(laptop -> {
            laptop.setStatus(LaptopStatusEnum.RENTED);
            laptopRepository.save(laptop);
        });
    }

    public void handleFailedRental(RentalOrder rental) {
        rental.setStatus(RentalStatusEnum.FAILED);
        rentalRepository.save(rental);
    }
}
