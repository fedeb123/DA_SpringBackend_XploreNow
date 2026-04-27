package com.XploreNowAPI.SpringAPI.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.XploreNowAPI.SpringAPI.application.dto.auth.ChangeEmailRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.InitiateEmailChangeRequest;
import com.XploreNowAPI.SpringAPI.application.dto.profile.ProfileResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.profile.UpdateProfileRequest;
import com.XploreNowAPI.SpringAPI.application.dto.profile.UpdateTravelPreferencesRequest;
import com.XploreNowAPI.SpringAPI.application.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Perfil del viajero autenticado")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Obtener perfil", description = "Retorna el perfil del usuario autenticado y resumen de reservas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ProfileResponseDto> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    @Operation(summary = "Actualizar perfil", description = "Actualiza datos basicos del perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Payload invalido", content = @Content(examples = @ExampleObject(value = "{\"message\":\"Validation failed\"}")))
    })
    public ResponseEntity<ProfileResponseDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Reemplazar preferencias de viaje", description = "Reemplaza completamente las preferencias de viaje del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias actualizadas"),
            @ApiResponse(responseCode = "400", description = "Payload invalido")
    })
    public ResponseEntity<ProfileResponseDto> replacePreferences(@Valid @RequestBody UpdateTravelPreferencesRequest request) {
        return ResponseEntity.ok(profileService.replaceTravelPreferences(request));
    }

    @PostMapping("/me/email-change/initiate")
    @Operation(summary = "Iniciar cambio de email", description = "Envía OTP al nuevo email para verificación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP enviado"),
            @ApiResponse(responseCode = "400", description = "Email invalido o ya en uso"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Void> initiateEmailChange(
            @Valid @RequestBody InitiateEmailChangeRequest request) {
        profileService.initiateEmailChange(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/email-change/confirm")
    @Operation(summary = "Confirmar cambio de email", description = "Verifica OTP y actualiza el email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email actualizado"),
            @ApiResponse(responseCode = "400", description = "OTP invalido o expirado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ProfileResponseDto> confirmEmailChange(
            @Valid @RequestBody ChangeEmailRequest request) {
        return ResponseEntity.ok(profileService.confirmEmailChange(request));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Eliminar cuenta", description = "Elimina permanentemente la cuenta y todos sus datos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Void> deleteAccount() {
        profileService.deleteAccount();
        return ResponseEntity.noContent().build();
    }
}
