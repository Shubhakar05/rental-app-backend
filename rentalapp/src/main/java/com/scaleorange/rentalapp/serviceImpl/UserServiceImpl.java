package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.*;
import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import com.scaleorange.rentalapp.repository.UsersRepository;
import com.scaleorange.rentalapp.service.CloudinaryService;
import com.scaleorange.rentalapp.service.UserService;
import com.scaleorange.rentalapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // -------------------- SUPER ADMIN SIGNUP --------------------
    @Override
    public SigninResponseDto signupSuperAdmin(SigninRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByRole(RoleEnum.SUPER_ADMIN)) {
            throw new RuntimeException("A Super Admin already exists. Cannot create another one.");
        }

        String shortUid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.SUPER_ADMIN)
                .uid(shortUid)
                .verificationStatus(VerificationStatusEnum.VERIFIED)
                .verified(true)
                .build();

        Users saved = userRepository.save(user);

        return SigninResponseDto.builder()
                .userId(saved.getUid())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(saved.getRole())
                .verificationStatus(saved.getVerificationStatus())
                .verified(saved.isVerified())
                .build();
    }

    // -------------------- VENDOR / COMPANY ADMIN SIGNUP --------------------
    @Override
    public SigninResponseDto signupAdmin(SigninRequestDto request, MultipartFile document) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        String documentUrl = null;
        if (document != null && !document.isEmpty()) {
            documentUrl = cloudinaryService.uploadFile(document);
        }

        String shortUid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()) // VENDOR_ADMIN or COMPANY_ADMIN
                .panNumber(request.getPanNumber())
                .gstNumber(request.getGstNumber())
                .mcaNumber(request.getMcaNumber())
                .documentPath(documentUrl)
                .uid(shortUid)
                .verificationStatus(VerificationStatusEnum.PENDING)
                .verified(false)
                .build();

        Users saved = userRepository.save(user);

        return SigninResponseDto.builder()
                .userId(saved.getUid())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(saved.getRole())
                .verificationStatus(saved.getVerificationStatus())
                .verified(saved.isVerified())
                .build();
    }

    // -------------------- LOGIN --------------------
    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        Optional<Users> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new LoginResponseDto("Invalid credentials", null, null, null, null);
        }

        Users user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new LoginResponseDto("Invalid credentials", null, null, null, null);
        }

        // Check verification status for non-super-admin users
        if (user.getRole() != RoleEnum.SUPER_ADMIN &&
                user.getVerificationStatus() != VerificationStatusEnum.VERIFIED) {
            return new LoginResponseDto("User not approved yet", null, null, null, null);
        }

        // Generate JWT token with a list of roles (even if single role)
        List<String> roles = List.of(user.getRole().name()); // Wrap single role in a list
        String token = jwtUtil.generateToken(user.getEmail(), roles);

        return new LoginResponseDto(
                "Login successful",
                user.getUid(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }

    // -------------------- APPROVE / REJECT --------------------
    @Override
    public UserDto approveUser(String uid) {
        Users user = userRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerificationStatus(VerificationStatusEnum.VERIFIED);
        user.setVerified(true);
        return toDto(userRepository.save(user));
    }

    @Override
    public UserDto rejectUser(String uid, String remarks) {
        Users user = userRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerificationStatus(VerificationStatusEnum.REJECTED);
        user.setVerified(false);
        return toDto(userRepository.save(user));
    }

    // -------------------- GET USERS BY STATUS --------------------
    @Override
    public List<UserDto> getUsersByStatus(VerificationStatusEnum status) {
        return userRepository.findByVerificationStatus(status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // -------------------- HELPER --------------------
    private UserDto toDto(Users user) {
        return UserDto.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .panNumber(user.getPanNumber())
                .gstNumber(user.getGstNumber())
                .mcaNumber(user.getMcaNumber())
                .documentPath(user.getDocumentPath())
                .verificationStatus(user.getVerificationStatus())
                .verified(user.isVerified())
                .build();
    }
}
