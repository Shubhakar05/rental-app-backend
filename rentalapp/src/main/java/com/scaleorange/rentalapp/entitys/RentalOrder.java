package com.scaleorange.rentalapp.entitys;

import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import jakarta.persistence.*;
import lombok.*;
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
    private Long id; // auto-increment

    @Column(unique = true, nullable = false)
    private String uid; // short unique string

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // Company Admin who rents

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
    private RentalStatusEnum status; // PENDING, CONFIRMED, COMPLETED, CANCELLED

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
