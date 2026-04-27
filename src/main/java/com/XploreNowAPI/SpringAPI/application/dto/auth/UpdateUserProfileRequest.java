package com.XploreNowAPI.SpringAPI.application.dto.auth;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserProfileRequest(

        @NotBlank
        @Size(max = 80)
        String firstName,

        @NotBlank
        @Size(max = 80)
        String lastName,

        @Size(max = 30)
        String phone,

        // Foto de perfil en Base64 (opcional)
        String profilePhoto,

        // Preferencias seleccionadas
        Set<ActivityCategory> preferences
) {
}