package com.XploreNowAPI.SpringAPI.application.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRatingRequest(
        @NotNull Long reservationId,
        @NotNull @Min(1) @Max(5) Integer activityStars,
        @NotNull @Min(1) @Max(5) Integer guideStars,
        @Size(max = 300) String comment
) {
}
