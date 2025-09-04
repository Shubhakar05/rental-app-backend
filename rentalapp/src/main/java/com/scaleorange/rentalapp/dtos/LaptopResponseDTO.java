package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaptopResponseDTO {

    private String laptopUuid;   // unique identifier for laptop
    private String brand;
    private String model;
    private double pricePerMonth;
    private String specs;
    private LaptopStatusEnum status; // AVAILABLE, RENTED, MAINTENANCE
}
