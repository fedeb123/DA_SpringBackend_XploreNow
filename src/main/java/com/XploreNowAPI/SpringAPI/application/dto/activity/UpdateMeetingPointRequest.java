package com.XploreNowAPI.SpringAPI.application.dto.activity;

public record UpdateMeetingPointRequest(
        String address,
        Double latitude,
        Double longitude
) {
}
