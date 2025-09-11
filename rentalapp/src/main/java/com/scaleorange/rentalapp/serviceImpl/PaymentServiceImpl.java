package com.scaleorange.rentalapp.serviceImpl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.scaleorange.rentalapp.dtos.PaymentCallbackDTO;
import com.scaleorange.rentalapp.dtos.PaymentResponseDTO;
import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import com.scaleorange.rentalapp.entitys.Payment;
import com.scaleorange.rentalapp.entitys.RentalOrder;
import com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum;
import com.scaleorange.rentalapp.enums.PaymentStatusEnum;
import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import com.scaleorange.rentalapp.repository.LaptopRentalTransactionRepository;
import com.scaleorange.rentalapp.repository.PaymentRepository;
import com.scaleorange.rentalapp.repository.RentalOrderRepository;
import com.scaleorange.rentalapp.service.LaptopService;
import com.scaleorange.rentalapp.service.PaymentService;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  // Import for @Transactional
import java.time.LocalDate;

import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalOrderRepository rentalOrderRepository;
    private final LaptopRentalTransactionRepository transactionRepository;
    private final LaptopService laptopService;

    private final RentalService rentalService;


    @Value("${razorpay.key_id}")
    private String razorpayKey;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              RentalOrderRepository rentalOrderRepository,
                              LaptopRentalTransactionRepository transactionRepository,
                              LaptopService laptopService, RentalService rentalService) {
        this.paymentRepository = paymentRepository;
        this.rentalOrderRepository = rentalOrderRepository;
        this.transactionRepository = transactionRepository;
        this.laptopService = laptopService;
        this.rentalService = rentalService;
    }

    @Override
    public PaymentResponseDTO createRazorpayOrder(Long rentalOrderId) {
        try {
            RentalOrder rentalOrder = rentalOrderRepository.findById(rentalOrderId)
                    .orElseThrow(() -> new RuntimeException("Rental order not found: " + rentalOrderId));

            Payment existingPayment = paymentRepository.findByRentalOrderId(rentalOrderId).orElse(null);
            if (existingPayment != null) {
                log.info("Existing payment found for rentalOrderId={}", rentalOrderId);
                return PaymentResponseDTO.builder()
                        .razorpayOrderId(existingPayment.getRazorpayOrderId())
                        .rentalUid(rentalOrder.getUid())
                        .amount(existingPayment.getAmount())
                        .currency("INR")
                        .key(razorpayKey)
                        .build();
            }

            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject options = new JSONObject();
            options.put("amount", rentalOrder.getTotalAmount().doubleValue() * 100); // paise
            options.put("currency", "INR");
            options.put("receipt", "order_rcptid_" + rentalOrder.getId());

            Order order = client.orders.create(options);

            Payment payment = new Payment();
            payment.setRazorpayOrderId(order.get("id"));
            payment.setRentalOrder(rentalOrder);
            payment.setAmount(rentalOrder.getTotalAmount());
            payment.setStatus(PaymentStatusEnum.PENDING);

            paymentRepository.save(payment);

            return PaymentResponseDTO.builder()
                    .razorpayOrderId(order.get("id"))
                    .rentalUid(rentalOrder.getUid())
                    .amount(rentalOrder.getTotalAmount())
                    .currency("INR")
                    .key(razorpayKey)
                    .build();
        } catch (Exception e) {
            log.error("Error while creating Razorpay order", e);
            throw new RuntimeException("Failed to create payment order", e);
        }
    }

    @Transactional
    @Override
    public String handlePaymentCallback(PaymentCallbackDTO callbackDto) {
        try {
            // 1️⃣ Verify Razorpay signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", callbackDto.getRazorpayOrderId());
            options.put("razorpay_payment_id", callbackDto.getRazorpayPaymentId());
            options.put("razorpay_signature", callbackDto.getRazorpaySignature());

            boolean isSignatureValid = Utils.verifyPaymentSignature(options, razorpaySecret);

            // 2️⃣ Fetch Payment entity
            Payment payment = paymentRepository.findByRazorpayOrderId(callbackDto.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // 3️⃣ Update payment info
            payment.setRazorpayPaymentId(callbackDto.getRazorpayPaymentId());
            payment.setRazorpaySignature(callbackDto.getRazorpaySignature());
            payment.setPaymentDate(LocalDate.now());
            payment.setStatus(isSignatureValid ? PaymentStatusEnum.SUCCESS : PaymentStatusEnum.FAILED);
            paymentRepository.save(payment);

            // 4️⃣ Fetch associated RentalOrder
            RentalOrder rentalOrder = payment.getRentalOrder();

            // 5️⃣ Delegate to RentalService for status + transactions + laptops
            if (isSignatureValid) {
                rentalService.markRentalAsPaid(rentalOrder);
                return "Payment successful";
            } else {
                rentalService.handleFailedRental(rentalOrder);
                return "Payment verification failed";
            }

        } catch (Exception e) {
            log.error("Error in handlePaymentCallback", e);
            return "Payment failed: " + e.getMessage();
        }
    }

}