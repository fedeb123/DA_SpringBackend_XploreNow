package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.reservation.CancelReservationResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.CreateReservationRequest;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.ReservationDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.ReservationSummaryDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ReservationEvent;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationChangeType;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationEventRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;
    private final ActivityScheduleRepository activityScheduleRepository;
    private final ReservationEventRepository reservationEventRepository;

    @Transactional
    public ReservationDetailDto createReservation(CreateReservationRequest request) {
        AppUser user = currentUserService.getCurrentUser();

        ActivitySchedule schedule = activityScheduleRepository.findByIdForUpdate(request.scheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Activity activity = schedule.getActivity();
        if (!activity.getId().equals(request.activityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule does not belong to the requested activity");
        }

        if (request.participantsCount() > schedule.getAvailableSpots()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough available spots");
        }

        schedule.setReservedSpots(schedule.getReservedSpots() + request.participantsCount());
        activityScheduleRepository.save(schedule);

        BigDecimal totalAmount = schedule.getPrice().multiply(BigDecimal.valueOf(request.participantsCount()));

        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .seats(request.participantsCount())
                .totalAmount(totalAmount)
                .status(ReservationStatus.CONFIRMED)
                .voucherCode(generateVoucherCode())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        saveEvent(saved, ReservationChangeType.CONFIRMED, "Reserva confirmada");

        return toDetailDto(saved);
    }

    @Transactional
    public CancelReservationResponseDto cancelReservation(Long reservationId) {
        AppUser user = currentUserService.getCurrentUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to the authenticated user");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation cannot be cancelled in its current status");
        }

        ActivitySchedule schedule = activityScheduleRepository.findByIdForUpdate(reservation.getSchedule().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        schedule.setReservedSpots(Math.max(0, schedule.getReservedSpots() - reservation.getSeats()));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        activityScheduleRepository.save(schedule);
        Reservation saved = reservationRepository.save(reservation);
        saveEvent(saved, ReservationChangeType.CANCELLED, "Reserva cancelada por el usuario");

        return new CancelReservationResponseDto(
                saved.getId(),
                saved.getStatus(),
                saved.getCancelledAt(),
                "Reserva cancelada correctamente"
        );
    }

    @Transactional(readOnly = true)
    public Page<ReservationSummaryDto> getMyReservations(ReservationStatus status, Pageable pageable) {
        AppUser user = currentUserService.getCurrentUser();
        Page<Reservation> page = status == null
                ? reservationRepository.findByUserId(user.getId(), pageable)
                : reservationRepository.findByUserIdAndStatus(user.getId(), status, pageable);

        return page.map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public ReservationDetailDto getReservationDetail(Long reservationId) {
        AppUser user = currentUserService.getCurrentUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to the authenticated user");
        }

        return toDetailDto(reservation);
    }

    private ReservationSummaryDto toSummaryDto(Reservation reservation) {
        Activity activity = reservation.getSchedule().getActivity();
        return new ReservationSummaryDto(
                reservation.getId(),
                activity.getName(),
                activity.getDestination().getName(),
                reservation.getSchedule().getStartDateTime().toLocalDate(),
                reservation.getSchedule().getStartDateTime().toLocalTime().format(TIME_FORMATTER),
                reservation.getSeats(),
                reservation.getStatus(),
                reservation.getVoucherCode()
        );
    }

    private ReservationDetailDto toDetailDto(Reservation reservation) {
        Activity activity = reservation.getSchedule().getActivity();
        return new ReservationDetailDto(
                reservation.getId(),
                activity.getName(),
                activity.getDestination().getName(),
                reservation.getSchedule().getStartDateTime().toLocalDate(),
                reservation.getSchedule().getStartDateTime().toLocalTime().format(TIME_FORMATTER),
                reservation.getSeats(),
                reservation.getStatus(),
                reservation.getVoucherCode(),
                activity.getMeetingPoint(),
                reservation.getTotalAmount(),
                activity.getCancellationPolicy()
        );
    }

    private String generateVoucherCode() {
        return "XPLR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void saveEvent(Reservation reservation, ReservationChangeType changeType, String detail) {
        ReservationEvent event = ReservationEvent.builder()
                .reservation(reservation)
                .changeType(changeType)
                .changedAt(LocalDateTime.now())
                .detail(detail)
                .build();
        reservationEventRepository.save(event);
    }
}
