package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.InvoiceResponseDTO;
import com.scaleorange.rentalapp.entitys.RentalOrder;
import com.scaleorange.rentalapp.service.InvoiceService;
import com.scaleorange.rentalapp.util.PdfGeneratorUtil;
import com.scaleorange.rentalapp.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final RentalOrderRepository rentalOrderRepository;

    /**
     * Endpoint to download invoice PDF for a given rental order UID.
     */
    @GetMapping("/download/{rentalUid}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String rentalUid) {

        // 1. Fetch rental order by UID
        RentalOrder rentalOrder = rentalOrderRepository.findByUid(rentalUid)
                .orElseThrow(() -> new RuntimeException("Rental order not found"));

        // 2. Generate Invoice DTO (calculates GST, sets vendor/customer details)
        InvoiceResponseDTO invoiceDto = invoiceService.generateInvoice(rentalOrder);

        // 3. Generate PDF bytes using Thymeleaf + Flying Saucer
        byte[] pdfBytes = pdfGeneratorUtil.generateInvoicePdf(invoiceDto);

        // 4. Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", invoiceDto.getInvoiceNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
