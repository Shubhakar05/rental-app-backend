package com.scaleorange.rentalapp.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentRequestDTO {
    private String rentalUid;
    private BigDecimal amount;
}
