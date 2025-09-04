package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RoleEnum;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequestDto {
    private String username;
    private String email;
    private String password;
    private RoleEnum role;

    // Optional verification details (used by Vendor/Company Admin)
    private String panNumber;
    private String gstNumber;
    private String mcaNumber;

    // Optional file for multipart uploads (Vendor/Company Admin)
    @ToString.Exclude // prevents printing large file in logs
    private transient MultipartFile document; // transient ensures JSON deserialization ignores it
}
