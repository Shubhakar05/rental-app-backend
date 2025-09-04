package com.scaleorange.rentalapp.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CartResponseDTO {
    private String cartUid;
    private String userUid;
    private List<String> laptopUids;

    // New fields for checkout
    private Long numberOfMonths;
    private BigDecimal totalAmount;
    private LocalDateTime rentalTime;
    private LocalDateTime returnTime;
}
