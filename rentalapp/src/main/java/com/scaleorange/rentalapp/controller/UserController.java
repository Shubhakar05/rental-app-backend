package com.scaleorange.rentalapp.controller;

import com.scaleorange.rentalapp.dtos.SigninRequestDto;
import com.scaleorange.rentalapp.dtos.SigninResponseDto;
import com.scaleorange.rentalapp.dtos.UserDto;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import com.scaleorange.rentalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------- SUPER ADMIN --------------------

    /**
     * Super Admin signup (JSON) - public endpoint, no verification needed.
     */
    @PostMapping("/signup/super-admin")
    public ResponseEntity<SigninResponseDto> signupSuperAdmin(@RequestBody SigninRequestDto request) {
        SigninResponseDto response = userService.signupSuperAdmin(request);
        return ResponseEntity.ok(response);
    }

    // -------------------- VENDOR / COMPANY ADMIN --------------------

    /**
     * Vendor / Company Admin signup (multipart) - public endpoint, requires document upload & verification.
     *
     * @param request  SigninRequestDto containing username, email, password, role (VENDOR_ADMIN / COMPANY_ADMIN)
     * @param document Optional verification document
     */
    @PostMapping(value = "/signup/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SigninResponseDto> signupAdmin(
            @ModelAttribute SigninRequestDto request,
            @RequestParam(required = false) MultipartFile document) {

        // service will handle role-based logic
        SigninResponseDto response = userService.signupAdmin(request, document);
        return ResponseEntity.ok(response);
    }

    // -------------------- USER APPROVAL / REJECTION --------------------

    /**
     * Approve user (Super Admin only) by UID.
     */
    @PutMapping("/{uid}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> approveUser(@PathVariable String uid) {
        UserDto response = userService.approveUser(uid);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject user (Super Admin only) by UID.
     */
    @PutMapping("/{uid}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> rejectUser(
            @PathVariable String uid,
            @RequestParam String remarks) {
        UserDto response = userService.rejectUser(uid, remarks);
        return ResponseEntity.ok(response);
    }

    // -------------------- GET USERS BY STATUS --------------------

    /**
     * Get all users by verification status (Super Admin only).
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByStatus(@PathVariable VerificationStatusEnum status) {
        List<UserDto> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }
}
