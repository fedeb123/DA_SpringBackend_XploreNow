package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import java.time.LocalDateTime;

public record RescheduleActivityRequest(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
