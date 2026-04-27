package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

public record ReservationDetailDto(
        Long reservationId,
        String activityName,
        String destination,
        LocalDate date,
        String time,
        Integer participantsCount,
        ReservationStatus status,
        String voucherCode,
        String meetingPoint,
        BigDecimal totalPrice,
        String cancellationPolicy
) {
}
