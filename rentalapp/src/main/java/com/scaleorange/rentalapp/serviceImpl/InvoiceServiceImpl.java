package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.InvoiceResponseDTO;
import com.scaleorange.rentalapp.entitys.Company;
import com.scaleorange.rentalapp.entitys.Invoice;
import com.scaleorange.rentalapp.entitys.RentalOrder;
import com.scaleorange.rentalapp.enums.InvoiceStatusEnum;
import com.scaleorange.rentalapp.repository.CompanyRepository;
import com.scaleorange.rentalapp.repository.InvoiceRepository;
import com.scaleorange.rentalapp.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;

    @Override
    public InvoiceResponseDTO generateInvoice(RentalOrder rentalOrder) {

        // --- Fetch vendor and customer company ---
        Company vendorCompany = companyRepository
                .findFirstByIsVendorTrueAndIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("Vendor company not found"));

        Company customerCompany = rentalOrder.getUser().getCompany();

        // --- Split GST and round to 2 decimals ---
        BigDecimal totalGst = rentalOrder.getTotalGst();
        BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        // --- Generate Invoice Number with date and time ---
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String invoiceNumber = "INV-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 8);

        // --- Build Invoice Entity ---
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .rentalOrder(rentalOrder)
                .subtotalAmount(rentalOrder.getBaseAmount())
                .cgstAmount(cgst)
                .sgstAmount(sgst)
                .totalAmount(rentalOrder.getTotalAmount())
                .status(InvoiceStatusEnum.PAID)
                .issuedAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        invoiceRepository.save(invoice);

        // --- Build DTO for PDF ---
        return InvoiceResponseDTO.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .issuedAt(invoice.getIssuedAt())
                .paidAt(invoice.getPaidAt())
                .vendorCompanyName(vendorCompany.getName())
                .vendorAddress(vendorCompany.getAddress())
                .vendorGstin(vendorCompany.getGstNumber())
                .customerCompanyName(customerCompany.getName())
                .customerAddress(customerCompany.getAddress())
                .customerGstin(customerCompany.getGstNumber())
                .subtotalAmount(invoice.getSubtotalAmount())
                .cgstAmount(invoice.getCgstAmount())
                .sgstAmount(invoice.getSgstAmount())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus().name())
                .build();
    }
}
