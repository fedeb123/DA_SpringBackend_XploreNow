package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import java.time.LocalDate;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

public record ReservationSummaryDto(
        Long reservationId,
        String activityName,
        String destination,
        LocalDate date,
        String time,
        Integer participantsCount,
        ReservationStatus status,
        String voucherCode
) {
}
