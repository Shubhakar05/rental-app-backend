package com.scaleorange.rentalapp.entitys;

import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.util.Base64;

@Entity
@Table(name = "rental_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToMany
    @JoinTable(
            name = "rental_order_laptops",
            joinColumns = @JoinColumn(name = "rental_order_id"),
            inverseJoinColumns = @JoinColumn(name = "laptop_id")
    )
    private List<Laptops> laptops;

    private LocalDateTime rentalTime;
    private LocalDateTime returnTime;

    @Enumerated(EnumType.STRING)
    private RentalStatusEnum status;

    @Column(nullable = false)
    private long numberOfMonths; // NEW: store rental duration

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount; // NEW: store total price

    @OneToMany(mappedBy = "rentalOrder", cascade = CascadeType.ALL)
    private List<Payment> payments;

    @OneToMany(mappedBy = "rentalOrder", cascade = CascadeType.ALL)
    private List<InvoiceLineItem> invoiceLineItems;

    @PrePersist
    public void prePersist() {
        if (uid == null) {
            uid = generateUid();
        }
    }

    private String generateUid() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        byte[] bytes = buffer.array();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 12);
    }
}
