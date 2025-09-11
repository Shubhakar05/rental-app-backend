package com.scaleorange.rentalapp.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class InvoiceRequestDTO {

    private Long rentalOrderId;           // RentalOrder FK
    private LocalDate periodStart;        // Invoice coverage start
    private LocalDate periodEnd;          // Invoice coverage end

    private BigDecimal subtotalAmount;    // Before tax
    private BigDecimal cgstAmount;        // CGST applied
    private BigDecimal sgstAmount;        // SGST applied
    private BigDecimal totalAmount;       // subtotal + taxes

    private String status;                // e.g. "PAID" or "DRAFT"

    // Vendor info (optional if you want redundancy for reporting)
    private Long vendorId;
    private String vendorCompanyName;
    private String vendorAddress;
    private String vendorGstin;

    // Customer info
    private Long customerId;
    private String customerCompanyName;
    private String customerAddress;
    private String customerGstin;
}
