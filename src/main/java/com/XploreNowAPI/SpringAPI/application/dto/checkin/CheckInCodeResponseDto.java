package com.XploreNowAPI.SpringAPI.application.dto.checkin;

import java.time.LocalDateTime;

public record CheckInCodeResponseDto(
        Long scheduleId,
        String qrContent,
        LocalDateTime expiresAt
) {
}