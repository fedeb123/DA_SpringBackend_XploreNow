package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.auth.UpdateUserProfileRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.UserProfileResponse;
import com.XploreNowAPI.SpringAPI.application.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints de perfil del viajero")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(
            summary = "Obtener perfil del usuario autenticado",
            description = "Retorna datos personales, preferencias y resumen de actividades reservadas y realizadas"
    )
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        UserProfileResponse response = profileService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(
            summary = "Actualizar perfil del usuario autenticado",
            description = "Permite actualizar nombre, apellido, teléfono, foto de perfil y preferencias de viaje"
    )
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse response = profileService.updateMyProfile(request);
        return ResponseEntity.ok(response);
    }
}