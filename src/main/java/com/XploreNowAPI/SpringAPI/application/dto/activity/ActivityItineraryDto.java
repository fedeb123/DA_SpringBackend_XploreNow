package com.XploreNowAPI.SpringAPI.application.dto.activity;

public record ActivityItineraryDto(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        Integer orderIndex
) {
}
