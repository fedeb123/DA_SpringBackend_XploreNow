package com.XploreNowAPI.SpringAPI.application.dto.history;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

import java.time.LocalDate;

public record HistoryItemDto(
        Long reservationId,
        String activityName,
        String destination,
        LocalDate date,
        String guideName,
        Integer durationMinutes,
        ReservationStatus status,
        Integer rating,
        boolean hasRating
) {
}
