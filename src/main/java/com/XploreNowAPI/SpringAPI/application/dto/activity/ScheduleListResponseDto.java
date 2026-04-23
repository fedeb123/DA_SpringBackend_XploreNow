package com.XploreNowAPI.SpringAPI.application.dto.activity;

import java.util.List;

public record ScheduleListResponseDto(
        List<ScheduleSummaryDto> content
) {
}
