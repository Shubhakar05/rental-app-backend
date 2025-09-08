package com.scaleorange.rentalapp.entitys;

import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name = "laptops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Laptops {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    private String brand;
    private String model;
    private double pricePerMonth;


    private String specs;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private LaptopStatusEnum status;

    @Column(nullable = true)
    private String vendorUid; // links laptop to vendor

    @PrePersist
    public void prePersist() {
        if (uid == null) uid = generateUid();
        if (status == null) status = LaptopStatusEnum.AVAILABLE;
        if (vendorUid == null) vendorUid = "default_vendor";
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
