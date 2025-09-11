package com.scaleorange.rentalapp.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponseDTO {

    private String invoiceNumber;        // From Invoice entity
    private LocalDateTime issuedAt;      // When invoice was issued
    private LocalDateTime paidAt;        // When payment was done

    // Vendor details (fetched from RentalOrder → Vendor)
    private String vendorCompanyName;
    private String vendorAddress;
    private String vendorGstin;

    // Customer details (fetched from RentalOrder → Customer)
    private String customerCompanyName;
    private String customerAddress;
    private String customerGstin;

    // Amounts
    private BigDecimal subtotalAmount;   // Before tax
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal totalAmount;      // subtotal + cgst + sgst

    private String status;               // InvoiceStatusEnum
}
