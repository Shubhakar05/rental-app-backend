package com.scaleorange.rentalapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPageController {

    @GetMapping("/invoice-download-page")
    public String invoiceDownloadPage() {
        // Return the Thymeleaf template name (without .html)
        return "invoice-download";
    }
}
