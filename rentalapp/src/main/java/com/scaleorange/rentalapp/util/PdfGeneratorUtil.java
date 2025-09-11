package com.scaleorange.rentalapp.util;

import com.scaleorange.rentalapp.dtos.InvoiceResponseDTO;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class PdfGeneratorUtil {

    private final TemplateEngine templateEngine;

    public PdfGeneratorUtil(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generates a PDF byte array from the invoice DTO.
     */
    public byte[] generateInvoicePdf(InvoiceResponseDTO invoiceDto) {
        try {
            // 1. Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("invoice", invoiceDto);

            // 2. Render HTML from Thymeleaf template
            String htmlContent = templateEngine.process("invoice", context);

            // 3. Generate PDF using Flying Saucer
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
