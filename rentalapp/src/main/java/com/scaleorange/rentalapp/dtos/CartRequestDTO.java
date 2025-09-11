package com.scaleorange.rentalapp.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartRequestDTO {
    private List<String> laptopUids;
    private long numberOfMonths;
    private LocalDateTime rentalTime;   // optional
    private LocalDateTime returnTime;   // optional
}
