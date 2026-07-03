package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import java.time.LocalDate;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

public record VoucherDto(
        Long reservationId,
        String activityName,
        LocalDate date,
        String time,
        String meetingPoint,
        String guideName,
        Integer participantsCount,
        ReservationStatus reservationStatus,
        boolean checkedIn
) {
}