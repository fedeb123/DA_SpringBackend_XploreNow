package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityItineraryDto;

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
        Double meetingPointLatitude,
        Double meetingPointLongitude,
        BigDecimal totalPrice,
        String cancellationPolicy,
        List<ActivityItineraryDto> itineraries
) {
}

