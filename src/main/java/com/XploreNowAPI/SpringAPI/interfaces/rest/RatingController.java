package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.rating.CreateRatingRequest;
import com.XploreNowAPI.SpringAPI.application.dto.rating.PendingRatingDto;
import com.XploreNowAPI.SpringAPI.application.dto.rating.RatingResponseDto;
import com.XploreNowAPI.SpringAPI.application.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Calificaciones de actividades y guías")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @Operation(summary = "Crear rating", description = "Crea una calificación para una reserva completada")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rating creado"),
            @ApiResponse(responseCode = "409", description = "Reserva ya calificada"),
            @ApiResponse(responseCode = "400", description = "Fuera de ventana de 48 horas o reserva no completada")
    })
    public ResponseEntity<RatingResponseDto> createRating(@Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.createRating(request));
    }

    @GetMapping("/pending")
    @Operation(summary = "Ratings pendientes", description = "Lista reservas completadas del usuario sin rating dentro de 48 horas")
    @ApiResponse(responseCode = "200", description = "Pendientes obtenidos")
    public ResponseEntity<List<PendingRatingDto>> getPendingRatings() {
        return ResponseEntity.ok(ratingService.getPendingRatings());
    }
}
