package com.XploreNowAPI.SpringAPI.application.dto.activity;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;

import java.math.BigDecimal;

public record ActivitySummaryDto(
        Long activityId,
        String image,
        String name,
        String destination,
        ActivityCategory category,
        Integer durationMinutes,
        BigDecimal price,
        Integer availableSpots
) {
}
