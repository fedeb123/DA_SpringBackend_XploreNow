package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.profile.ProfileResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.profile.ReservationSummaryCounterDto;
import com.XploreNowAPI.SpringAPI.application.dto.profile.UpdateProfileRequest;
import com.XploreNowAPI.SpringAPI.application.dto.profile.UpdateTravelPreferencesRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.TravelPreferenceType;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ReservationRepository reservationRepository;

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
}
