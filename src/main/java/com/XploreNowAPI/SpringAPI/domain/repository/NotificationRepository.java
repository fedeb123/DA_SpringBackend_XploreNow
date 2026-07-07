package com.XploreNowAPI.SpringAPI.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Notification;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    List<Notification> findByDeliverAtBeforeAndStatus(LocalDateTime time, NotificationStatus status);
}
