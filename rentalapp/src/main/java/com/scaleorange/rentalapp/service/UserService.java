package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.SigninRequestDto;
import com.scaleorange.rentalapp.dtos.SigninResponseDto;
import com.scaleorange.rentalapp.dtos.LoginRequestDto;
import com.scaleorange.rentalapp.dtos.LoginResponseDto;
import com.scaleorange.rentalapp.dtos.UserDto;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    // -------------------- SIGNUP --------------------

    /**
     * Signup Super Admin (JSON) - no verification needed.
     */
    SigninResponseDto signupSuperAdmin(SigninRequestDto request);

    /**
     * Signup Vendor / Company Admin (multipart) - requires verification & optional document.
     * request.role should indicate "VENDOR_ADMIN" or "COMPANY_ADMIN"
     */
    SigninResponseDto signupAdmin(SigninRequestDto request, MultipartFile document);

    // -------------------- LOGIN --------------------

    /**
     * Login (Vendor Admin / Company Admin / Super Admin).
     */
    LoginResponseDto login(LoginRequestDto request);

    // -------------------- APPROVAL / REJECTION --------------------

    /**
     * Approve a user (Super Admin only) by UID.
     */
    UserDto approveUser(String uid);

    /**
     * Reject a user (Super Admin only) by UID.
     */
    UserDto rejectUser(String uid, String remarks);

    // -------------------- FETCH USERS --------------------

    /**
     * Get all users with specific verification status.
     */
    List<UserDto> getUsersByStatus(VerificationStatusEnum status);
}
