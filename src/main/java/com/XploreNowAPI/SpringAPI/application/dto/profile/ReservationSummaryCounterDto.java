package com.XploreNowAPI.SpringAPI.application.dto.profile;

public record ReservationSummaryCounterDto(
        long confirmed,
        long cancelled,
        long completed
) {
}
