package com.scaleorange.rentalapp.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponseDTO {
    private String rentalUid;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String key;
}
