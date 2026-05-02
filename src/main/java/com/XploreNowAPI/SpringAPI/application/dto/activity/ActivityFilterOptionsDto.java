package com.XploreNowAPI.SpringAPI.application.dto.activity;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;

import java.util.List;

public record ActivityFilterOptionsDto(
        List<DestinationOptionDto> destinations,
        List<ActivityCategory> categories
) {
}
