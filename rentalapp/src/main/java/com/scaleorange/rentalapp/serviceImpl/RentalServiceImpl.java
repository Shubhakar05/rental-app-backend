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

    @Override
    public RentalResponseDTO createRental(RentalRequestDTO request, String userUid) {
        Users user = usersRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Laptops> laptops = laptopRepository.findAllById(request.getLaptopIds());

        if (laptops.isEmpty()) {
            throw new RuntimeException("No laptops found for the given IDs");
        }

        // Lock laptops for immediate rental
        laptops.forEach(laptop -> {
            if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                throw new RuntimeException("Laptop not available: " + laptop.getUid());
            }
            laptop.setStatus(LaptopStatusEnum.LOCKED);
            laptop.setLockTime(LocalDateTime.now());
            laptopRepository.save(laptop);
        });

        LocalDateTime rentalTime = request.getRentalTime() != null ? request.getRentalTime() : LocalDateTime.now();
        LocalDateTime returnTime = rentalTime.plusMonths(request.getNumberOfMonths());

        // Calculate total amount
        BigDecimal totalAmount = laptops.stream()
                .map(laptop -> BigDecimal.valueOf(laptop.getPricePerMonth())) // convert double -> BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(request.getNumberOfMonths()));


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
    public void createRentalForCart(String userUid, List<Laptops> laptops, long numberOfMonths,
                                    LocalDateTime rentalTime, LocalDateTime returnTime, BigDecimal totalAmount) {
        Users user = usersRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RentalOrder rental = RentalOrder.builder()
                .user(user)
                .laptops(laptops)
                .status(RentalStatusEnum.ACTIVE)
                .numberOfMonths(numberOfMonths)
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .totalAmount(totalAmount)
                .build();

        rentalRepository.save(rental);

        // Update laptop status to RENTED
        laptops.forEach(laptop -> {
            laptop.setStatus(LaptopStatusEnum.RENTED);
            laptopRepository.save(laptop);
        });
    }
}
