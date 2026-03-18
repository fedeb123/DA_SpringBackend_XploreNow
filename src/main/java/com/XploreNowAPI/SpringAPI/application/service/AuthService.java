package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.auth.AuthResponse;
import com.XploreNowAPI.SpringAPI.application.dto.auth.ClassicLoginRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpChallengeResponse;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpVerifyRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.RegisterRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.OtpVerification;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Role;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpStatus;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.RoleType;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.OtpVerificationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.RoleRepository;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpRepository;
    private final OtpDeliveryService otpDeliveryService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${xplorenow.otp.expiration-minutes:10}")
    private long otpExpirationMinutes;

    @Value("${xplorenow.otp.max-attempts:5}")
    private int otpMaxAttempts;

    @Transactional
    public AuthResponse registerClassic(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Role travelerRole = roleRepository.findByName(RoleType.TRAVELER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.TRAVELER).build()));

        AppUser newUser = AppUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .phone(request.phone() == null ? null : request.phone().trim())
                .enabled(true)
                .build();

        newUser.getRoles().add(travelerRole);
        AppUser saved = userRepository.save(newUser);

        return buildAuthResponse(saved);
    }

    public AuthResponse classicLogin(ClassicLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        AppUser user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        return buildAuthResponse(user);
    }

    @Transactional
    public OtpChallengeResponse requestOtp(OtpRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        AppUser user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        expirePendingOtps(normalizedEmail, request.purpose());

        String plainCode = generateOtpCode();
        LocalDateTime now = LocalDateTime.now();
        OtpVerification otp = OtpVerification.builder()
                .user(user)
                .email(normalizedEmail)
                .codeHash(passwordEncoder.encode(plainCode))
                .purpose(request.purpose())
                .status(OtpStatus.PENDING)
                .expiresAt(now.plusMinutes(otpExpirationMinutes))
                .attempts(0)
                .maxAttempts(otpMaxAttempts)
                .build();

        otpRepository.save(otp);
        otpDeliveryService.sendOtp(normalizedEmail, plainCode, request.purpose());

        return new OtpChallengeResponse(
                normalizedEmail,
                request.purpose().name(),
                otpExpirationMinutes * 60,
                "OTP sent successfully"
        );
    }

    @Transactional
    public OtpChallengeResponse resendOtp(OtpRequest request) {
        return requestOtp(request);
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        OtpVerification otp = otpRepository
                .findTopByEmailIgnoreCaseAndPurposeAndStatusOrderByCreatedAtDesc(
                        normalizedEmail,
                        request.purpose(),
                        OtpStatus.PENDING
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No pending OTP for this email"));

        validateOtpState(otp);

        if (!passwordEncoder.matches(request.code(), otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= otp.getMaxAttempts()) {
                otp.setStatus(OtpStatus.EXPIRED);
            }
            otpRepository.save(otp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP code");
        }

        otp.setStatus(OtpStatus.CONSUMED);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        AppUser user = Optional.ofNullable(otp.getUser())
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(normalizedEmail)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                jwtService.getExpirationSeconds(),
                "Bearer",
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName()
        );
    }

    private void validateOtpState(OtpVerification otp) {
        if (otp.getStatus() != OtpStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is not active");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is expired");
        }

        if (otp.getAttempts() >= otp.getMaxAttempts()) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP max attempts reached");
        }
    }

    private void expirePendingOtps(String email, com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose purpose) {
        List<OtpVerification> pendingOtps = otpRepository.findByEmailIgnoreCaseAndPurposeAndStatus(
                email,
                purpose,
                OtpStatus.PENDING
        );

        for (OtpVerification pending : pendingOtps) {
            pending.setStatus(OtpStatus.EXPIRED);
        }

        if (!pendingOtps.isEmpty()) {
            otpRepository.saveAll(pendingOtps);
        }
    }

    private String generateOtpCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
