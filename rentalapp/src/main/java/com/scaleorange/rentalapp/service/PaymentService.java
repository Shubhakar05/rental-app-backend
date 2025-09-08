package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.PaymentCallbackDTO;
import com.scaleorange.rentalapp.dtos.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO createRazorpayOrder(Long rentalOrderId);
    String handlePaymentCallback(PaymentCallbackDTO callbackDto);
}
