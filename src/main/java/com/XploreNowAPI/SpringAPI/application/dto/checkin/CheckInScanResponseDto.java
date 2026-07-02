package com.XploreNowAPI.SpringAPI.application.dto.checkin;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.CheckInStatus;

import java.time.LocalDateTime;

public record CheckInScanResponseDto(
        CheckInStatus status,
        Long reservationId,
        String activityName,
        LocalDateTime scannedAt,
        String message
) {
}