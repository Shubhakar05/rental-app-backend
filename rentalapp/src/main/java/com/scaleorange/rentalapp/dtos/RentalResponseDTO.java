package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
public class RentalResponseDTO {
    private String rentalUid;
    private String userUid;
    private List<String> laptopUids;
    private String brand;
    private LocalDateTime rentalTime;
    private LocalDateTime returnTime;
    private RentalStatusEnum status;

    // Finance info
    private long numberOfMonths;
    private BigDecimal baseAmount;   // amount before GST
    private BigDecimal totalGst;     // total GST applied
    private BigDecimal totalAmount;  // baseAmount + totalGst
}
