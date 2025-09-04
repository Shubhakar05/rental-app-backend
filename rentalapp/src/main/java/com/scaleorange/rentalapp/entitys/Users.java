package com.scaleorange.rentalapp.entitys;

import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;


    private String panNumber;
    private String gstNumber;
    private String mcaNumber;
    private String documentPath;

    @Enumerated(EnumType.STRING)
    private VerificationStatusEnum verificationStatus;

    private boolean verified;

    // Link to the company the user belongs to (vendor or customer)
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    // Auto-generate UID before saving
    @PrePersist
    public void prePersist() {
        if (this.uid == null) {
            this.uid = generateUid();
        }
    }

    private String generateUid() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 12);
    }
}
