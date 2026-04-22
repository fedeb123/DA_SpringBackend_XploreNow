package com.XploreNowAPI.SpringAPI.application.dto.history;

import java.time.LocalDate;

public record HistoryItemDto(
        Long reservationId,
        String activityName,
        String destination,
        LocalDate date,
        String guideName,
        Integer durationMinutes,
        Integer rating,
        boolean hasRating
) {
}
