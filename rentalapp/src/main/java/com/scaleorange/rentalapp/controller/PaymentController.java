package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.PaymentCallbackDTO;
import com.scaleorange.rentalapp.dtos.PaymentResponseDTO;
import com.scaleorange.rentalapp.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController  // Use RestController for JSON responses
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Return payment details as JSON for frontend JS
    @GetMapping("/raz/{rentalOrderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentDetails(@PathVariable("rentalOrderId") Long rentalOrderId) {
        try {
            log.info("Fetching Razorpay order for rentalOrderId={}", rentalOrderId);
            PaymentResponseDTO paymentResponse = paymentService.createRazorpayOrder(rentalOrderId);
            return ResponseEntity.ok(paymentResponse);
        } catch (RuntimeException e) {
            log.error("Error creating Razorpay order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Handle Razorpay payment callback
    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(@RequestBody PaymentCallbackDTO callbackDto) {
        try {
            String result = paymentService.handlePaymentCallback(callbackDto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Payment processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Payment processing failed: " + e.getMessage());
        }
    }
}
