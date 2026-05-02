package com.XploreNowAPI.SpringAPI.application.dto.activity;

import java.math.BigDecimal;

public record SavedActivityCheckDto(
        Long activityId,
        BigDecimal price,
        Integer availableSpots,
        String currency
) {
}
