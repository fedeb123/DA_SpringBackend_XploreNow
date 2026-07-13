package com.XploreNowAPI.SpringAPI.interfaces.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.XploreNowAPI.SpringAPI.application.dto.reservation.CancelActivityResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.CancelReservationResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.CreateReservationRequest;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.RescheduleActivityRequest;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.RescheduleActivityResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.ReservationDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.ReservationSummaryDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.VoucherDto;
import com.XploreNowAPI.SpringAPI.application.service.CheckInService;
import com.XploreNowAPI.SpringAPI.application.service.ReservationService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Gestión de reservas del usuario autenticado")
public class ReservationController {

    private final ReservationService reservationService;
    private final CheckInService checkInService;

    @PostMapping
        @Operation(summary = "Crear reserva", description = "Confirma una reserva descontando cupos del schedule. El scheduleId debe obtenerse desde GET /api/v1/activities/{activityId}/schedules")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada"),
            @ApiResponse(responseCode = "400", description = "Schedule no pertenece a la actividad"),
            @ApiResponse(responseCode = "404", description = "Schedule no encontrado"),
            @ApiResponse(responseCode = "409", description = "Sin cupos suficientes")
    })
    public ResponseEntity<ReservationDetailDto> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva confirmada y devuelve cupos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada"),
            @ApiResponse(responseCode = "403", description = "Reserva de otro usuario"),
            @ApiResponse(responseCode = "400", description = "Estado no cancelable")
    })
    public ResponseEntity<CancelReservationResponseDto> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @PostMapping("/activities/{scheduleId}/cancel")
    @Operation(summary = "Cancelar actividad para todos los inscritos", description = "Cancela todas las reservas confirmadas asociadas a un schedule y notifica a los usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad cancelada"),
            @ApiResponse(responseCode = "404", description = "Schedule no encontrado")
    })
    public ResponseEntity<CancelActivityResponseDto> cancelActivityReservations(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(reservationService.cancelActivityReservations(scheduleId));
    }

    @PostMapping("/activities/{scheduleId}/reschedule")
    @Operation(summary = "Reprogramar actividad para todos los inscritos", description = "Actualiza el horario de un schedule y notifica a todos los usuarios con reservas confirmadas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad reprogramada"),
            @ApiResponse(responseCode = "400", description = "Fechas inválidas"),
            @ApiResponse(responseCode = "404", description = "Schedule no encontrado")
    })
    public ResponseEntity<RescheduleActivityResponseDto> rescheduleActivityReservations(
            @PathVariable Long scheduleId,
            @Valid @RequestBody RescheduleActivityRequest request) {
        return ResponseEntity.ok(reservationService.rescheduleActivityReservations(scheduleId, request));
    }

    @GetMapping("/my")
    @Operation(summary = "Listar mis reservas", description = "Lista paginada de reservas del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Reservas obtenidas")
    public ResponseEntity<Page<ReservationSummaryDto>> getMyReservations(
            @Parameter(description = "Estado de reserva")
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(reservationService.getMyReservations(status, pageable));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Detalle de reserva", description = "Obtiene el detalle de una reserva del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle obtenido"),
            @ApiResponse(responseCode = "403", description = "Reserva de otro usuario")
    })
    public ResponseEntity<ReservationDetailDto> getReservationDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getReservationDetail(reservationId));
    }

    @GetMapping("/{reservationId}/voucher")
    @Operation(summary = "Obtener voucher digital", description = "Retorna el voucher digital de una reserva confirmada o completada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher obtenido"),
            @ApiResponse(responseCode = "403", description = "Reserva de otro usuario"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reserva sin voucher válido en su estado actual")
    })
    public ResponseEntity<VoucherDto> getVoucher(@PathVariable Long reservationId) {
        return ResponseEntity.ok(checkInService.getVoucher(reservationId));
    }
}
