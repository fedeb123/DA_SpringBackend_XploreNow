package com.XploreNowAPI.SpringAPI.application.dto.history;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

import java.time.LocalDate;

public record HistoryDetailDto(
        Long reservationId,
        String activityName,
        String destination,
        LocalDate date,
        String guideName,
        Integer durationMinutes,
        ReservationStatus status,
        String meetingPoint,
        String cancellationPolicy,
        Integer activityStars,
        Integer guideStars,
        String comment,
        boolean hasRating
) {
}
