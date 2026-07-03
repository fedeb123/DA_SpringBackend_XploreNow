package com.XploreNowAPI.SpringAPI.application.dto.checkin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckInScanRequest(
        @NotNull Long reservationId,
        @NotBlank String qrContent
) {
}