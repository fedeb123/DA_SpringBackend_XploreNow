package com.XploreNowAPI.SpringAPI.application.dto.reservation;

public record CancelActivityResponseDto(
        Long scheduleId,
        int cancelledReservations,
        String message
) {
}
