package com.XploreNowAPI.SpringAPI.application.dto.profile;

import java.util.List;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.TravelPreferenceType;

import jakarta.validation.constraints.NotNull;

public record UpdateTravelPreferencesRequest(
        @NotNull List<TravelPreferenceType> preferences
) {
}
