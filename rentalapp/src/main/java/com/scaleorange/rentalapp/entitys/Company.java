package com.scaleorange.rentalapp.entitys;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "gst_number", unique = true)
    private String gstNumber;

    @Column(nullable = false)
    private Boolean isVendor = false;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Invoices issued by this company (vendor)
    @OneToMany(mappedBy = "issuerCompany")
    private List<Invoice> issuedInvoices;

    // Invoices received by this company (customer)
    @OneToMany(mappedBy = "receiverCompany")
    private List<Invoice> receivedInvoices;

    // Payments made by this company (customer)
    @OneToMany(mappedBy = "payerCompany")
    private List<Payment> paymentsMade;

    // Payments received by this company (vendor)
    @OneToMany(mappedBy = "payeeCompany")
    private List<Payment> paymentsReceived;
}
