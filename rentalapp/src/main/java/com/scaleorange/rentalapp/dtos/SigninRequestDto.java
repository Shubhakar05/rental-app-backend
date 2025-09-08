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

    // Company-related details (needed during signup)
    private String companyName;
    private String gstNumber;
    private String panNumber;
    private String mcaNumber;

    private String address;
    private String contactEmail;
    private String contactPhone;
    private String state;

    // Optional file upload (like GST/PAN certificate, docs)
    @ToString.Exclude
    private transient MultipartFile document;
}
