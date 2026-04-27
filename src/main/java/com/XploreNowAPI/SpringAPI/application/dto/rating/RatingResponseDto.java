package com.XploreNowAPI.SpringAPI.application.dto.rating;

import java.time.LocalDateTime;

public record RatingResponseDto(
        Long ratingId,
        Long reservationId,
        Integer activityStars,
        Integer guideStars,
        String comment,
        LocalDateTime createdAt
) {
}
