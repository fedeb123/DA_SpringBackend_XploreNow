package com.XploreNowAPI.SpringAPI.application.dto.activity;

public record ActivityItineraryCreateRequest(
        String name,
        Double latitude,
        Double longitude,
        Integer orderIndex
) {
}
