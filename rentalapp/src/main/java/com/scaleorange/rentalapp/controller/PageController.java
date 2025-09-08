package com.scaleorange.rentalapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/payment-page")
    public String paymentPage() {
        return "payment"; // Thymeleaf will render payment.html from templates
    }
}
