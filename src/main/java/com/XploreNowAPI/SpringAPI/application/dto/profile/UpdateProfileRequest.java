package com.XploreNowAPI.SpringAPI.application.dto.profile;

import java.util.Set;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank
        @Size(max = 80)
        String firstName,

        @NotBlank
        @Size(max = 80)
        String lastName,

        @Size(max = 30)
        String phone,

        @Size(max = 500)
        String profilePictureUrl,

        Set<ActivityCategory> preferences

) {
}