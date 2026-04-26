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

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;

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
}

