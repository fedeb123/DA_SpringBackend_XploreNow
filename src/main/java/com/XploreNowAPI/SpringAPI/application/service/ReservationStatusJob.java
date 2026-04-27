package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ReservationEvent;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationChangeType;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationEventRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationStatusJob {

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migratePendingReservationsToConfirmed() {
        reservationRepository.migrateStatuses("PENDING", "CONFIRMED");
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void markCompletedReservations() {
        List<Reservation> reservations = reservationRepository.findByStatusWithEndedScheduleBefore(
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );

        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationEventRepository.save(ReservationEvent.builder()
                    .reservation(reservation)
                    .changeType(ReservationChangeType.COMPLETED)
                    .changedAt(LocalDateTime.now())
                    .detail("Reserva marcada como completada automaticamente")
                    .build());
        }

        reservationRepository.saveAll(reservations);
    }
}