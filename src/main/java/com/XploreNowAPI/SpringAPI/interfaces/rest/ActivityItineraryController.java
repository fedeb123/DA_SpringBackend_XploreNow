package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityItineraryCreateRequest;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityItineraryDto;
import com.XploreNowAPI.SpringAPI.application.service.ActivityItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities/{activityId}/itineraries")
@RequiredArgsConstructor
@Tag(name = "Activity Itineraries", description = "Gestión de puntos del itinerario de una actividad")
public class ActivityItineraryController {

    private final ActivityItineraryService activityItineraryService;

    @GetMapping
    @Operation(summary = "Obtener itinerarios", description = "Retorna todos los puntos del itinerario de una actividad, ordenados por índice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerarios obtenidos"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })
    public ResponseEntity<List<ActivityItineraryDto>> getItineraries(
            @Parameter(description = "ID de la actividad")
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(activityItineraryService.getActivityItineraries(activityId));
    }

    @PostMapping
    @Operation(summary = "Crear punto de itinerario", description = "Agrega un nuevo punto al itinerario de una actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Punto creado"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })
    public ResponseEntity<ActivityItineraryDto> createItinerary(
            @Parameter(description = "ID de la actividad")
            @PathVariable Long activityId,
            @RequestBody ActivityItineraryCreateRequest request
    ) {
        ActivityItineraryDto result = activityItineraryService.createItinerary(activityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{itineraryId}")
    @Operation(summary = "Actualizar punto de itinerario", description = "Actualiza los datos de un punto del itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Punto actualizado"),
            @ApiResponse(responseCode = "404", description = "Punto no encontrado")
    })
    public ResponseEntity<ActivityItineraryDto> updateItinerary(
            @Parameter(description = "ID de la actividad")
            @PathVariable Long activityId,
            @Parameter(description = "ID del punto de itinerario")
            @PathVariable Long itineraryId,
            @RequestBody ActivityItineraryCreateRequest request
    ) {
        ActivityItineraryDto result = activityItineraryService.updateItinerary(itineraryId, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{itineraryId}")
    @Operation(summary = "Eliminar punto de itinerario", description = "Elimina un punto del itinerario de una actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Punto eliminado"),
            @ApiResponse(responseCode = "404", description = "Punto no encontrado")
    })
    public ResponseEntity<Void> deleteItinerary(
            @Parameter(description = "ID de la actividad")
            @PathVariable Long activityId,
            @Parameter(description = "ID del punto de itinerario")
            @PathVariable Long itineraryId
    ) {
        activityItineraryService.deleteItinerary(itineraryId);
        return ResponseEntity.noContent().build();
    }
}
