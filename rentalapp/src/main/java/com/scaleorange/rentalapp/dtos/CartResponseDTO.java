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

    // Checkout info
    private Long numberOfMonths;
    private BigDecimal totalAmount;
    private LocalDateTime rentalTime;
    private LocalDateTime returnTime;

    // Optional billing breakdown
    private BigDecimal baseAmount;    // before GST
    private BigDecimal cgst;          // CGST amount
    private BigDecimal sgst;          // SGST amount
    private BigDecimal depositAmount; // security deposit
    private BigDecimal lateFee;       // currently zero, can be updated later
}
