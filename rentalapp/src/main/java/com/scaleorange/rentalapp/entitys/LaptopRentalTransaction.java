package com.scaleorange.rentalapp.entitys;

import com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "laptop_rental_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class    LaptopRentalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    // Link to the main rental order
    @ManyToOne
    @JoinColumn(name = "rental_order_id", nullable = false)
    private RentalOrder rentalOrder;

    // Vendor who owns the laptop
    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Users vendor;

    // Consumer renting the laptop
    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    private Users consumer;

    // Rental period (from/to)
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;

    @ManyToOne
    @JoinColumn(name = "laptop_uid", nullable = false)
    private Laptops laptop;


    // Financial details
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;      // rent before GST

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cgst;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sgst;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal depositAmount;

    private LocalDateTime actualReturnDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lateFee;

    // --- Deposit & Refund tracking ---
    private Boolean depositRefunded;                   // true = refunded, false = withheld
    private BigDecimal depositRefundAmount;            // actual refunded amount
    private LocalDateTime refundDate;                  // when refund was processed
    private String refundId;                           // Razorpay refund reference

    // --- Damage handling ---
    private Boolean isDamaged;                         // quick flag
    private BigDecimal damageCost;                     // damage amount assessed
    private BigDecimal extraChargeAmount;              // if damageCost > deposit
    private String inspectionRemarks;                  // vendor notes on return

    // --- Invoice tracking ---
    private Boolean invoiceGenerated;                  // has invoice been generated
    private String paymentId;                          // Razorpay payment reference



    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    // base + gst + deposit + late fee

    // Transaction status
    @Enumerated(EnumType.STRING)
    private LaptopTransactionStatusEnum status;
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (uid == null) uid = generateUid();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (status == null) status = LaptopTransactionStatusEnum.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateUid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
