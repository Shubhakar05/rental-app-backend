package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String uid;
    private String username;
    private String email;
    private RoleEnum role;
    private String panNumber;
    private String gstNumber;
    private String mcaNumber;
    private String documentPath;
    private VerificationStatusEnum verificationStatus;
    private boolean verified;
}
