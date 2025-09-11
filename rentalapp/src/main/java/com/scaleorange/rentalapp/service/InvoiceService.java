package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.InvoiceResponseDTO;
import com.scaleorange.rentalapp.entitys.RentalOrder;

public interface InvoiceService {
    InvoiceResponseDTO generateInvoice(RentalOrder rentalOrder);
}
