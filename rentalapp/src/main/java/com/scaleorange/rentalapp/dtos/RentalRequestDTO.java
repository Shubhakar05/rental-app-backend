package com.scaleorange.rentalapp.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RentalRequestDTO {

    private String brand;
    private List<String> laptopUids;
    private LocalDateTime rentalTime;
    private long numberOfMonths;
}
