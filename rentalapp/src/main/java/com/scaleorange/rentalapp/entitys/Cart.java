package com.scaleorange.rentalapp.entitys;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.util.Base64;

@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // auto-increment

    @Column(unique = true, nullable = false)
    private String uid; // short unique string

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToMany
    @JoinTable(
            name = "cart_laptops",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "laptop_id")
    )
    private List<Laptops> laptops;

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
