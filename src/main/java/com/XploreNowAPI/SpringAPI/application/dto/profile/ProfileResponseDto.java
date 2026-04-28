package com.XploreNowAPI.SpringAPI.application.dto.profile;

import java.util.List;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;

public record ProfileResponseDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profilePictureUrl,
        List<ActivityCategory> preferences,
        ReservationSummaryCounterDto reservationSummary
) {
}