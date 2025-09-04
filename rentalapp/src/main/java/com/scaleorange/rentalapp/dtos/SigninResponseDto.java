package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SigninResponseDto {
    private String userId;
    private String username;
    private String email;
    private RoleEnum role;
    private VerificationStatusEnum verificationStatus;
    private boolean verified;
}
