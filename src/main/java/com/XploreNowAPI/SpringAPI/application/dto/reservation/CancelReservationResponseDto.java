package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

import java.time.LocalDateTime;

public record CancelReservationResponseDto(
        Long reservationId,
        ReservationStatus status,
        LocalDateTime cancelledAt,
        String message
) {
}
