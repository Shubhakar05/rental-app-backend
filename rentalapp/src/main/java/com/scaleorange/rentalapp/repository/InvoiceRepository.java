package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByUuid(UUID uuid);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
