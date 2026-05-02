package com.XploreNowAPI.SpringAPI.application.dto.activity;

import java.util.List;

public record SavedActivityCheckResponseDto(
        List<SavedActivityCheckDto> content
) {
}
