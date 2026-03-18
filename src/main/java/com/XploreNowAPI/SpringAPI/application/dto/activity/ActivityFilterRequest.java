package com.XploreNowAPI.SpringAPI.application.dto.activity;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityFilterRequest(
        Long destinationId,
        ActivityCategory category,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
