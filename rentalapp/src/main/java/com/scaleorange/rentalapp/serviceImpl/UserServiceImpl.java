package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.*;
import com.scaleorange.rentalapp.entitys.Company;
import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.enums.RoleEnum;
import com.scaleorange.rentalapp.enums.VerificationStatusEnum;
import com.scaleorange.rentalapp.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;
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

        String uid = generateUid();

        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.SUPER_ADMIN)
                .uid(uid)
                .verificationStatus(VerificationStatusEnum.VERIFIED)
                .verified(true)
                .build();

        Users saved = userRepository.save(user);
        return toSigninResponse(saved);
    }

    // -------------------- VENDOR / COMPANY ADMIN SIGNUP --------------------
    @Override
    public SigninResponseDto signupAdmin(SigninRequestDto request, MultipartFile document) {
        // 1️⃣ Check for existing user
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // 2️⃣ Upload document (if any)
        String documentUrl = null;
        if (document != null && !document.isEmpty()) {
            documentUrl = cloudinaryService.uploadFile(document);
        }

        // 3️⃣ Create Company entity
        Company company = Company.builder()
                .name(request.getCompanyName())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .mcaNumber(request.getMcaNumber())
                .address(request.getAddress())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .state(request.getState())
                .isVendor(request.getRole() == RoleEnum.VENDOR_ADMIN)
                .isActive(true)
                .documentPath(documentUrl)
                .build();

        Company savedCompany = companyRepository.save(company);

        // 4️⃣ Generate UID for User
        String uid = generateUid();

        // 5️⃣ Create User entity with reference to saved Company
        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()) // VENDOR_ADMIN or COMPANY_ADMIN
                .uid(uid)
                .verificationStatus(VerificationStatusEnum.PENDING)
                .verified(false)
                .company(savedCompany)
                .build();

        Users savedUser = userRepository.save(user);

        // 6️⃣ Return response
        return toSigninResponse(savedUser);
    }

    // -------------------- LOGIN --------------------
    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        Optional<Users> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new LoginResponseDto("Invalid credentials", null, null, null, null);
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new LoginResponseDto("Invalid credentials", null, null, null, null);
        }

        if (user.getRole() != RoleEnum.SUPER_ADMIN &&
                user.getVerificationStatus() != VerificationStatusEnum.VERIFIED) {
            return new LoginResponseDto("User not approved yet", null, null, null, null);
        }

        List<String> roles = List.of(user.getRole().name());
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

    // -------------------- HELPERS --------------------
    private String generateUid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "").substring(0, 12);
    }

    // Updated method to include company details
    private SigninResponseDto toSigninResponse(Users user) {
        Company company = user.getCompany(); // may be null for SUPER_ADMIN

        return SigninResponseDto.builder()
                .userId(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .verificationStatus(user.getVerificationStatus())
                .verified(user.isVerified())
                .companyId(company != null ? company.getId() : null)
                .companyName(company != null ? company.getName() : null)
                .gstNumber(company != null ? company.getGstNumber() : null)
                .panNumber(company != null ? company.getPanNumber() : null)
                .mcaNumber(company != null ? company.getMcaNumber() : null)
                .address(company != null ? company.getAddress() : null)
                .contactEmail(company != null ? company.getContactEmail() : null)
                .contactPhone(company != null ? company.getContactPhone() : null)
                .state(company != null ? company.getState() : null)
                .build();
    }

    private UserDto toDto(Users user) {
        Company company = user.getCompany();

        return UserDto.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .verificationStatus(user.getVerificationStatus())
                .verified(user.isVerified())
                .companyId(company != null ? company.getId() : null)
                .companyName(company != null ? company.getName() : null)
                .gstNumber(company != null ? company.getGstNumber() : null)
                .panNumber(company != null ? company.getPanNumber() : null)
                .mcaNumber(company != null ? company.getMcaNumber() : null)
                .address(company != null ? company.getAddress() : null)
                .contactEmail(company != null ? company.getContactEmail() : null)
                .contactPhone(company != null ? company.getContactPhone() : null)
                .state(company != null ? company.getState() : null)
                .build();
    }
}
