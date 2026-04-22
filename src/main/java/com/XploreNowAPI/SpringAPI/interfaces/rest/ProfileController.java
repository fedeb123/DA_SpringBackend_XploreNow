package com.XploreNowAPI.SpringAPI.interfaces.rest;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
