package com.XploreNowAPI.SpringAPI.application.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
        @NotNull Long activityId,
        @NotNull Long scheduleId,
        @NotNull @Min(1) Integer participantsCount
) {
}
