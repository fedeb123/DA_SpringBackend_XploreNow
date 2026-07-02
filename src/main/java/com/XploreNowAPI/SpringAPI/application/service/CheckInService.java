package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInCodeResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanRequest;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.VoucherDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.CheckIn;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.CheckInStatus;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.CheckInRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;
    private final ActivityScheduleRepository activityScheduleRepository;
    private final CheckInRepository checkInRepository;
    private final QrCodeSigner qrCodeSigner;

    @Value("${checkin.qr.expiration-minutes:180}")
    private long expirationMinutes;

    @Transactional(readOnly = true)
    public VoucherDto getVoucher(Long reservationId) {
        Reservation reservation = getOwnedReservation(reservationId);
        ensureVoucherAllowed(reservation);
        return toVoucherDto(reservation);
    }

    @Transactional(readOnly = true)
    public CheckInCodeResponseDto generateCheckInCode(Long scheduleId) {
        ActivitySchedule schedule = activityScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        if (!schedule.getEndDateTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "El horario ya venció");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        String qrContent = qrCodeSigner.sign(schedule.getId(), expiresAt.atZone(ZoneId.systemDefault()).toInstant());

        return new CheckInCodeResponseDto(schedule.getId(), qrContent, expiresAt);
    }

    @Transactional
    public CheckInScanResponseDto scan(CheckInScanRequest request) {
        QrCodeSigner.ParsedQrPayload payload;
        try {
            payload = qrCodeSigner.verify(request.qrContent());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "QR inválido");
        }

        if (qrCodeSigner.isExpired(payload)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "QR expirado, pedile uno nuevo al guía");
        }

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        AppUser currentUser = currentUserService.getCurrentUser();
        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to the authenticated user");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation must be confirmed to check in");
        }

        if (!reservation.getSchedule().getId().equals(payload.scheduleId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Este QR no corresponde a tu actividad");
        }

        if (checkInRepository.existsByReservationIdAndStatus(reservation.getId(), CheckInStatus.CONFIRMED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya registraste tu asistencia");
        }

        LocalDateTime scannedAt = LocalDateTime.now();
        CheckIn checkIn = CheckIn.builder()
                .reservation(reservation)
                .schedule(reservation.getSchedule())
                .status(CheckInStatus.CONFIRMED)
                .scannedAt(scannedAt)
                .build();
        checkInRepository.save(checkIn);

        Activity activity = reservation.getSchedule().getActivity();
        return new CheckInScanResponseDto(
                CheckInStatus.CONFIRMED,
                reservation.getId(),
                activity.getName(),
                scannedAt,
                "Asistencia confirmada"
        );
    }

    private Reservation getOwnedReservation(Long reservationId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to the authenticated user");
        }

        return reservation;
    }

    private void ensureVoucherAllowed(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation does not have a valid voucher in its current status");
        }
    }

    private VoucherDto toVoucherDto(Reservation reservation) {
        Activity activity = reservation.getSchedule().getActivity();
        return new VoucherDto(
                reservation.getId(),
                activity.getName(),
                reservation.getSchedule().getStartDateTime().toLocalDate(),
            reservation.getSchedule().getStartDateTime().toLocalTime().format(TIME_FORMATTER),
                activity.getMeetingPoint(),
                activity.getGuide().getUser().getFirstName() + " " + activity.getGuide().getUser().getLastName(),
                reservation.getSeats(),
                reservation.getStatus(),
                checkInRepository.existsByReservationIdAndStatus(reservation.getId(), CheckInStatus.CONFIRMED)
        );
    }
}