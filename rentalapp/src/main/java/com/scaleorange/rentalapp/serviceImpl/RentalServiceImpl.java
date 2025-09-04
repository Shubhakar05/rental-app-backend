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
        // Fetch user from JWT
        Users user = usersRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Example: pick laptop by brand & price
        List<Laptops> laptops = laptopRepository.findByBrandAndPricePerMonthAndStatus(
                request.getBrand(),
                request.getPricePerMonth(),
                LaptopStatusEnum.AVAILABLE
        );

        if (laptops.isEmpty()) {
            throw new RuntimeException("No available laptops found");
        }

        Laptops selectedLaptop = laptops.get(0);
        selectedLaptop.setStatus(LaptopStatusEnum.LOCKED);
        selectedLaptop.setLockTime(LocalDateTime.now());
        laptopRepository.save(selectedLaptop);

        RentalOrder rental = RentalOrder.builder()
                .user(user)
                .laptops(List.of(selectedLaptop))
                .status(RentalStatusEnum.PENDING)
                .rentalTime(request.getRentalTime() != null ? request.getRentalTime() : LocalDateTime.now())
                .returnTime(request.getReturnTime())
                .build();

        rentalRepository.save(rental);

        return RentalResponseDTO.builder()
                .rentalUid(rental.getUid())
                .userUid(user.getUid())
                .laptopUids(List.of(selectedLaptop.getUid()))
                .status(rental.getStatus())
                .rentalTime(rental.getRentalTime())
                .returnTime(rental.getReturnTime())
                .build();
    }

    @Override
    public void createRentalForCart(String userUid, List<Laptops> laptops) {
        // Fetch user from JWT
        Users user = usersRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lock all laptops
        laptops.forEach(laptop -> {
            if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                throw new RuntimeException("Laptop not available: " + laptop.getUid());
            }
            laptop.setStatus(LaptopStatusEnum.LOCKED);
            laptop.setLockTime(LocalDateTime.now());
            laptopRepository.save(laptop);
        });

        RentalOrder rental = RentalOrder.builder()
                .user(user)
                .laptops(laptops)
                .status(RentalStatusEnum.PENDING)
                .rentalTime(LocalDateTime.now())
                .build();

        rentalRepository.save(rental);
    }
}
