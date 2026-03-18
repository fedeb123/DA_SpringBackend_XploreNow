package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityFilterRequest;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivitySummaryDto;
import com.XploreNowAPI.SpringAPI.application.service.ActivityQueryService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Catalogo, destacadas y detalle de actividades")
public class ActivityController {

    private final ActivityQueryService activityQueryService;

    @GetMapping
        @Operation(summary = "Listado paginado de actividades", description = "Permite filtros combinados por destino, categoria, fecha y rango de precio")
    public ResponseEntity<Page<ActivitySummaryDto>> getCatalog(
            @Parameter(description = "ID del destino")
            @RequestParam(required = false) Long destinationId,
            @Parameter(description = "Categoria de actividad")
            @RequestParam(required = false) ActivityCategory category,
            @Parameter(description = "Fecha de la actividad (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Precio minimo")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Precio maximo")
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        ActivityFilterRequest filter = new ActivityFilterRequest(
                destinationId,
                category,
                date,
                minPrice,
                maxPrice
        );

        return ResponseEntity.ok(activityQueryService.getCatalog(filter, pageable));
    }

    @GetMapping("/{activityId}")
    @Operation(summary = "Detalle de actividad", description = "Retorna descripcion, guia, punto de encuentro, politicas, cupos y galeria")
    public ResponseEntity<ActivityDetailDto> getDetail(@PathVariable Long activityId) {
        return ResponseEntity.ok(activityQueryService.getActivityDetail(activityId));
    }

    @GetMapping("/featured")
    @Operation(summary = "Actividades destacadas", description = "Obtiene actividades recomendadas segun preferencias de usuario")
    public ResponseEntity<Page<ActivitySummaryDto>> getFeatured(
            @Parameter(description = "ID del usuario")
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(activityQueryService.getFeaturedForUser(userId, pageable));
    }
}
