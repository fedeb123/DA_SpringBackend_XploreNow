package com.XploreNowAPI.SpringAPI.application.dto.activity;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityLanguage;

import java.math.BigDecimal;
import java.util.List;

public record ActivityDetailDto(
        Long activityId,
        String name,
        ActivityCategory category,
        String shortDescription,
        String fullDescription,
        String destination,
        String guideName,
        Integer durationMinutes,
        ActivityLanguage language,
        String meetingPoint,
        String inclusions,
        String cancellationPolicy,
        BigDecimal price,
        String currency,
        Integer availableSpots,
        List<String> gallery
) {
}
