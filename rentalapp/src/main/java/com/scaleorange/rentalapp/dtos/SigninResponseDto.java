package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SigninResponseDto {
    // User info
    private String userId;
    private String username;
    private String email;
    private RoleEnum role;
    private VerificationStatusEnum verificationStatus;
    private boolean verified;

    // Company info
    private Long companyId;
    private String companyName;
    private String gstNumber;
    private String panNumber;
    private String mcaNumber;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String state;
}
