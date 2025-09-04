package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String message;
    private String userId; // UID
    private String username;
    private RoleEnum role;
    private String token;
}
