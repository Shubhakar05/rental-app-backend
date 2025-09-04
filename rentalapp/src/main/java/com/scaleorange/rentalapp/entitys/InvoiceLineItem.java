package com.scaleorange.rentalapp.entitys;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "rental_order_id")
    private RentalOrder rentalOrder;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountExGst;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal gstAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountIncGst;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}