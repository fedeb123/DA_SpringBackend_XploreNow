package com.XploreNowAPI.SpringAPI.application.dto.rating;

import java.time.LocalDateTime;

public record PendingRatingDto(
        Long reservationId,
        String activityName,
        LocalDateTime completedAt,
        LocalDateTime expiresAt
) {
}
