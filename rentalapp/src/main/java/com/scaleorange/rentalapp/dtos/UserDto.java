package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String uid;
    private Long companyId;
    private String companyName;
    private String username;
    private String email;
    private RoleEnum role;
    private String panNumber;
    private String gstNumber;
    private String mcaNumber;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String state;
    private VerificationStatusEnum verificationStatus;
    private boolean verified;
}
