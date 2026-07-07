package com.XploreNowAPI.SpringAPI.infrastructure.notifications;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import com.XploreNowAPI.SpringAPI.application.dto.notification.NotificationDto;

@Component
public class PollingRegistry {

    private final ConcurrentHashMap<Long, DeferredResult<List<NotificationDto>>> map = new ConcurrentHashMap<>();

    public void register(Long userId, DeferredResult<List<NotificationDto>> deferred) {
        map.put(userId, deferred);
        deferred.onCompletion(() -> map.remove(userId));
        deferred.onTimeout(() -> map.remove(userId));
    }

    public void notifyUser(Long userId, List<NotificationDto> notifications) {
        var deferred = map.remove(userId);
        if (deferred != null) {
            deferred.setResult(notifications);
        }
    }
}
