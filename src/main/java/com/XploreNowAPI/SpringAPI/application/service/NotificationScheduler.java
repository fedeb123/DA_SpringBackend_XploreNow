package com.XploreNowAPI.SpringAPI.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Notification;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationType;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.NotificationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Corre segun lo que diga app properties
    @Scheduled(fixedDelayString = "${notifications.reminder-sweep-interval}")
    @Transactional
    public void schedule24hReminders() {
        LocalDateTime from = LocalDateTime.now().plusHours(23).plusMinutes(30);
        LocalDateTime to = LocalDateTime.now().plusHours(25).plusMinutes(30);

        // Fetch all reservations and filter confirmed ones within the (overlapping) 24h window
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> r.getSchedule() != null && r.getSchedule().getStartDateTime() != null)
                .filter(r -> !r.getSchedule().getStartDateTime().isBefore(from) && r.getSchedule().getStartDateTime().isBefore(to))
                .toList();

        for (Reservation r : reservations) {
            // check if notification already exists
            List<Notification> exists = notificationRepository.findByUserIdAndStatus(r.getUser().getId(), com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationStatus.PENDING);
            boolean already = exists.stream().anyMatch(n -> n.getReservation() != null && n.getReservation().getId().equals(r.getId()) && n.getType() == NotificationType.REMINDER);
            if (!already) {
                var payload = "Recordatorio: tu actividad '" + r.getSchedule().getActivity().getName() + "' es en 24 horas. Voucher: " + r.getVoucherCode();
                notificationService.createScheduled(r.getUser(), r, NotificationType.REMINDER, payload, r.getSchedule().getStartDateTime().minusHours(24));
            }
        }
    }
}
