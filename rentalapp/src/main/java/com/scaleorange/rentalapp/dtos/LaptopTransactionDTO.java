package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LaptopTransactionDTO {
    private String uid;
    private String laptopUid;
    private String rentalOrderUid;
    private String vendorUid;
    private String consumerUid;

    // Rental period
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;
    private LocalDateTime actualReturnDate;

    // Financials
    private BigDecimal baseAmount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal totalGst;
    private BigDecimal depositAmount;
    private BigDecimal lateFee;
    private BigDecimal totalAmount;

    // Deposit / Refund
    private Boolean depositRefunded;
    private BigDecimal depositRefundAmount;
    private LocalDateTime refundDate;
    private String refundId;

    // Damage handling
    private Boolean isDamaged;
    private BigDecimal damageCost;
    private BigDecimal extraChargeAmount;
    private String inspectionRemarks;

    // Invoice / Payment tracking
    private Boolean invoiceGenerated;
    private String paymentId;

    // Status
    private LaptopTransactionStatusEnum status;
}
