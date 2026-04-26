package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.auth.UpdateUserProfileRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.UserProfileResponse;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.XploreNowAPI.SpringAPI.application.dto.auth.ChangeEmailRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.InitiateEmailChangeRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.OtpVerification;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.OtpVerificationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import com.XploreNowAPI.SpringAPI.application.service.AuthService;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Locale;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;

    private final OtpVerificationRepository otpVerificationRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        AppUser user = getAuthenticatedUser();
        return mapToResponse(user);
    }

    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        AppUser user = getAuthenticatedUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());

        /*
         * IMPORTANTE:
         * Esto requiere que AppUser tenga:
         *
         * @Column(name = "profile_photo", columnDefinition = "TEXT")
         * private String profilePhoto;
         */
        user.setProfilePhoto(request.profilePhoto());

        /*
         * Reemplazar preferencias anteriores:
         * se limpia todo y se reconstruye.
         */
        user.getPreferences().clear();

        if (request.preferences() != null && !request.preferences().isEmpty()) {
            for (ActivityCategory category : request.preferences()) {
                UserPreference preference = UserPreference.builder()
                        .user(user)
                        .preferredCategory(category)
                        .destination(null) // por ahora solo categoría
                        .build();

                user.getPreferences().add(preference);
            }
        }

        AppUser savedUser = appUserRepository.save(user);

        return mapToResponse(savedUser);
    }

    private AppUser getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String email = authentication.getName();

        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));
    }

    private UserProfileResponse mapToResponse(AppUser user) {

        Long reservedActivitiesCount =
                reservationRepository.countByUserAndStatusIn(
                        user,
                        Set.of(
                                ReservationStatus.PENDING,
                                ReservationStatus.CONFIRMED
                        )
                );

        Long completedActivitiesCount =
                reservationRepository.countByUserAndStatus(
                        user,
                        ReservationStatus.COMPLETED
                );

        Set<ActivityCategory> preferences =
                user.getPreferences()
                        .stream()
                        .map(UserPreference::getPreferredCategory)
                        .collect(Collectors.toSet());

        return new UserProfileResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getProfilePhoto(),
                preferences,
                reservedActivitiesCount,
                completedActivitiesCount
        );
    }

    public void initiateEmailChange(InitiateEmailChangeRequest request) {
        AppUser user = getAuthenticatedUser();
        String newEmail = request.newEmail().trim().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        OtpRequest otpRequest = new OtpRequest(newEmail, OtpPurpose.CHANGE_EMAIL);
        authService.requestOtp(otpRequest);
    }

    public void confirmEmailChange(ChangeEmailRequest request) {
        AppUser user = getAuthenticatedUser();
        String newEmail = request.newEmail().trim().toLowerCase(Locale.ROOT);

        OtpVerification otp = otpVerificationRepository
                .findTopByEmailIgnoreCaseAndPurposeAndStatusOrderByCreatedAtDesc(
                        newEmail,
                        OtpPurpose.CHANGE_EMAIL,
                        OtpStatus.PENDING
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No pending OTP for this email"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpVerificationRepository.save(otp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        if (!passwordEncoder.matches(request.code(), otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= otp.getMaxAttempts()) {
                otp.setStatus(OtpStatus.EXPIRED);
            }
            otpVerificationRepository.save(otp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP code");
        }

        otp.setStatus(OtpStatus.CONSUMED);
        otp.setVerifiedAt(LocalDateTime.now());
        otpVerificationRepository.save(otp);

        if (appUserRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        user.setEmail(newEmail);
        appUserRepository.save(user);
    }

    public void deleteAccount() {
        AppUser user = getAuthenticatedUser();

        otpVerificationRepository.deleteAllByUser(user);
        reservationRepository.deleteAllByUser(user);

        appUserRepository.delete(user);
    }
}

