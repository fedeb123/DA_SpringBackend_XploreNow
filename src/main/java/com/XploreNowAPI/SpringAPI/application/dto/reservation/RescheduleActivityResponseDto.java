package com.XploreNowAPI.SpringAPI.application.dto.reservation;

public record RescheduleActivityResponseDto(
        Long scheduleId,
        int notifiedReservations,
        String message
) {
}
