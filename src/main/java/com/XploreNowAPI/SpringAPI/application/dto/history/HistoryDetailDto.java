package com.XploreNowAPI.SpringAPI.application.dto.history;

import java.time.LocalDate;

public record HistoryDetailDto(
        Long reservationId,
        String activityName,
        String destination,
        LocalDate date,
        String guideName,
        Integer durationMinutes,
        String meetingPoint,
        String cancellationPolicy,
        Integer activityStars,
        Integer guideStars,
        String comment,
        boolean hasRating
) {
}
