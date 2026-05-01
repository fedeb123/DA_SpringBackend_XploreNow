package com.XploreNowAPI.SpringAPI.application.dto.news;

import jakarta.validation.constraints.NotNull;

public record UpdateNewsStatusRequest(
        @NotNull(message = "active is required")
        Boolean active
) {
}
