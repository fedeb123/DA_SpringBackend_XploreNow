package com.XploreNowAPI.SpringAPI.application.dto.activity;

import java.time.LocalDate;

public record ActivityHistoryDto(
        Long activityId,
        String name,
        LocalDate date,
        String destination,
        String guideName,
        Integer durationMinutes
) {}
