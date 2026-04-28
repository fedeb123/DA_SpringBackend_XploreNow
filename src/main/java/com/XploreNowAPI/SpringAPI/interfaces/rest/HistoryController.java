package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.history.HistoryDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.history.HistoryItemDto;
import com.XploreNowAPI.SpringAPI.application.service.HistoryService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activity/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "Historial de actividades del usuario")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @Operation(summary = "Listar historial", description = "Retorna reservas del usuario con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    public ResponseEntity<Page<HistoryItemDto>> getHistory(
            @Parameter(description = "Fecha desde (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Fecha hasta (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "ID de destino")
            @RequestParam(required = false) Long destinationId,
            @Parameter(description = "Estado de la reserva. Puede repetirse para filtrar multiples estados")
            @RequestParam(required = false) List<ReservationStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(historyService.getHistory(fromDate, toDate, destinationId, status, pageable));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Detalle de historial", description = "Retorna detalle de la reserva y rating si existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de historial obtenido"),
            @ApiResponse(responseCode = "403", description = "Reserva de otro usuario")
    })
    public ResponseEntity<HistoryDetailDto> getHistoryDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(historyService.getHistoryDetail(reservationId));
    }
}
