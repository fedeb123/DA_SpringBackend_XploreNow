package com.XploreNowAPI.SpringAPI.application.dto.activity;

import java.time.LocalDate;

public record ScheduleSummaryDto(
        Long scheduleId,
        LocalDate date,
        String time,
        Integer availableSpots,
        Integer totalSpots
) {
}