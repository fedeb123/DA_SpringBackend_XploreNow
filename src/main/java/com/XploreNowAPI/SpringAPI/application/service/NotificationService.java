package com.XploreNowAPI.SpringAPI.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.XploreNowAPI.SpringAPI.application.dto.notification.NotificationDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Notification;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationStatus;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationType;
import com.XploreNowAPI.SpringAPI.domain.repository.NotificationRepository;
import com.XploreNowAPI.SpringAPI.infrastructure.notifications.PollingRegistry;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;
    private final PollingRegistry pollingRegistry;

    @Transactional
    public Notification createImmediate(AppUser user, Reservation reservation, NotificationType type, String payload) {
        Notification n = Notification.builder()
                .user(user)
                .reservation(reservation)
                .type(type)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .createdAt(LocalDateTime.now())
                .build();
        var saved = notificationRepository.save(n);
        // notify any waiting long-poll client for this user
        try {
            var dto = new NotificationDto(saved.getId(), saved.getType(), saved.getPayload(), saved.getDeliverAt());
            pollingRegistry.notifyUser(user.getId(), List.of(dto));
        } catch (Exception e) {
            // ignore
        }
        return saved;
    }

    @Transactional
    public Notification createScheduled(AppUser user, Reservation reservation, NotificationType type, String payload, LocalDateTime deliverAt) {
        Notification n = Notification.builder()
                .user(user)
                .reservation(reservation)
                .type(type)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .deliverAt(deliverAt)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> pollPendingForCurrentUser() {
        var user = currentUserService.getCurrentUser();
        var list = notificationRepository.findByUserIdAndStatus(user.getId(), NotificationStatus.PENDING);
        return list.stream().map(n -> new NotificationDto(n.getId(), n.getType(), n.getPayload(), n.getDeliverAt())).toList();
    }

    @Transactional
    public void markDelivered(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setStatus(NotificationStatus.DELIVERED);
            n.setDeliveredAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }
}
