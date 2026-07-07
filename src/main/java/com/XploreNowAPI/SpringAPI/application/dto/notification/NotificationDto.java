package com.XploreNowAPI.SpringAPI.application.dto.notification;

import java.time.LocalDateTime;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationType;

public record NotificationDto(
        Long id,
        NotificationType type,
        String payload,
        LocalDateTime deliverAt
) {}
