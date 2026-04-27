package com.XploreNowAPI.SpringAPI.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.XploreNowAPI.SpringAPI.application.dto.auth.ChangeEmailRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.InitiateEmailChangeRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpVerifyRequest;
import com.XploreNowAPI.SpringAPI.application.dto.profile.ProfileResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.profile.ReservationSummaryCounterDto;
import com.XploreNowAPI.SpringAPI.application.dto.profile.UpdateProfileRequest;
import com.XploreNowAPI.SpringAPI.application.dto.profile.UpdateTravelPreferencesRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.TravelPreferenceType;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.OtpVerificationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.UserPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ReservationRepository reservationRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile() {
        AppUser user = currentUserService.getCurrentUser();
        return toResponse(user);
    }

    @Transactional
    public ProfileResponseDto updateProfile(UpdateProfileRequest request) {
        AppUser user = currentUserService.getCurrentUser();

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        user.setProfilePictureUrl(request.profilePictureUrl() == null ? null : request.profilePictureUrl().trim());

        AppUser saved = appUserRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public ProfileResponseDto replaceTravelPreferences(UpdateTravelPreferencesRequest request) {
        AppUser user = currentUserService.getCurrentUser();

        userPreferenceRepository.deleteByUserIdAndTravelPreferenceTypeIsNotNull(user.getId());

        List<TravelPreferenceType> preferences = request.preferences() == null ? List.of() : request.preferences();
        for (TravelPreferenceType preferenceType : preferences.stream().distinct().toList()) {
            UserPreference preference = UserPreference.builder()
                    .user(user)
                    .travelPreferenceType(preferenceType)
                    .build();
            userPreferenceRepository.save(preference);
        }

        return toResponse(user);
    }

    private ProfileResponseDto toResponse(AppUser user) {
        List<TravelPreferenceType> travelPreferences = userPreferenceRepository
                .findByUserIdAndTravelPreferenceTypeIsNotNull(user.getId())
                .stream()
                .map(UserPreference::getTravelPreferenceType)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        ReservationSummaryCounterDto summary = new ReservationSummaryCounterDto(
                reservationRepository.countByUserIdAndStatus(user.getId(), ReservationStatus.CONFIRMED),
                reservationRepository.countByUserIdAndStatus(user.getId(), ReservationStatus.CANCELLED),
                reservationRepository.countByUserIdAndStatus(user.getId(), ReservationStatus.COMPLETED)
        );

        return new ProfileResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getProfilePictureUrl(),
                travelPreferences,
                summary
        );
    }

    @Transactional
    public void initiateEmailChange(InitiateEmailChangeRequest request) {
        AppUser user = currentUserService.getCurrentUser();
        String newEmail = request.newEmail().trim().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        authService.requestOtpForEmail(user, newEmail, OtpPurpose.CHANGE_EMAIL);
    }

    @Transactional
    public ProfileResponseDto confirmEmailChange(ChangeEmailRequest request) {
        AppUser user = currentUserService.getCurrentUser();
        String newEmail = request.newEmail().trim().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        authService.verifyOtp(new OtpVerifyRequest(newEmail, request.code(), OtpPurpose.CHANGE_EMAIL));

        user.setEmail(newEmail);
        appUserRepository.save(user);
        return getProfile();
    }

    @Transactional
    public void deleteAccount() {
        AppUser user = currentUserService.getCurrentUser();

        otpVerificationRepository.deleteAllByUser(user);
        reservationRepository.deleteAllByUser(user);

        appUserRepository.delete(user);
    }
}
