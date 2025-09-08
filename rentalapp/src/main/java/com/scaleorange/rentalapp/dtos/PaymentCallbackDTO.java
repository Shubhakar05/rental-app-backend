package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.PaymentStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCallbackDTO {
    private String rentalUid;
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
}
