package com.XploreNowAPI.SpringAPI.application.dto.auth;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;

import java.util.Set;

public record UserProfileResponse(
        String email,
        String firstName,
        String lastName,
        String phone,

        // Foto de perfil en Base64 (opcional)
        String profilePhoto,

        // Preferencias de viaje
        Set<ActivityCategory> preferences,

        // Resumen de actividades
        Long reservedActivitiesCount,
        Long completedActivitiesCount
) {
}