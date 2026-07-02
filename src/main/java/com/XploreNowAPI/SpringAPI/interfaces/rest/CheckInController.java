package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInCodeResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanRequest;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanResponseDto;
import com.XploreNowAPI.SpringAPI.application.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Check-in", description = "Voucher digital, QR de guía y confirmación de asistencia")
public class CheckInController {

    private final CheckInService checkInService;

    @GetMapping("/schedules/{scheduleId}/checkin-code")
    @Operation(summary = "Generar código QR de check-in", description = "Retorna el contenido firmando del QR para que el guía lo exhiba en el punto de encuentro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código generado"),
            @ApiResponse(responseCode = "404", description = "Schedule no encontrado"),
            @ApiResponse(responseCode = "422", description = "Schedule vencido")
    })
    public ResponseEntity<CheckInCodeResponseDto> getCheckInCode(@PathVariable Long scheduleId) {
        // TODO: restringir a rol GUIDE cuando exista
        return ResponseEntity.ok(checkInService.generateCheckInCode(scheduleId));
    }

    @PostMapping("/checkin/scan")
    @Operation(summary = "Escanear QR y confirmar asistencia", description = "Valida el QR mostrado por el guía y registra el check-in del viajero")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asistencia confirmada"),
            @ApiResponse(responseCode = "403", description = "Reserva de otro usuario"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto de negocio"),
            @ApiResponse(responseCode = "422", description = "QR inválido, expirado o que no corresponde a la reserva")
    })
    public ResponseEntity<CheckInScanResponseDto> scan(@Valid @RequestBody CheckInScanRequest request) {
        return ResponseEntity.ok(checkInService.scan(request));
    }
}