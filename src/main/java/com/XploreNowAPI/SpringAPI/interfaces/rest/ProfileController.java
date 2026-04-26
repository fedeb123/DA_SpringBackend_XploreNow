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

import com.XploreNowAPI.SpringAPI.application.dto.auth.ChangeEmailRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.InitiateEmailChangeRequest;
import org.springframework.http.HttpStatus;

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

    @PostMapping("/me/email-change/initiate")
    @Operation(summary = "Iniciar cambio de email", description = "Envía OTP al nuevo email para verificación")
    public ResponseEntity<Void> initiateEmailChange(
            @Valid @RequestBody InitiateEmailChangeRequest request) {
        profileService.initiateEmailChange(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/email-change/confirm")
    @Operation(summary = "Confirmar cambio de email", description = "Verifica OTP y actualiza el email")
    public ResponseEntity<Void> confirmEmailChange(
            @Valid @RequestBody ChangeEmailRequest request) {
        profileService.confirmEmailChange(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Eliminar cuenta", description = "Elimina permanentemente la cuenta y todos sus datos")
    public ResponseEntity<Void> deleteAccount() {
        profileService.deleteAccount();
        return ResponseEntity.noContent().build();
    }
}