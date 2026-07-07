package com.XploreNowAPI.SpringAPI.interfaces.rest;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.XploreNowAPI.SpringAPI.application.dto.notification.NotificationDto;
import com.XploreNowAPI.SpringAPI.application.service.CurrentUserService;
import com.XploreNowAPI.SpringAPI.application.service.NotificationService;
import com.XploreNowAPI.SpringAPI.infrastructure.notifications.PollingRegistry;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PollingRegistry pollingRegistry;
    private final CurrentUserService currentUserService;

    @PostMapping("/poll")
    public DeferredResult<ResponseEntity<List<NotificationDto>>> poll() {
        // check immediately
        var list = notificationService.pollPendingForCurrentUser();
        DeferredResult<ResponseEntity<List<NotificationDto>>> deferred = new DeferredResult<>(55_000L);
        if (!list.isEmpty()) {
            deferred.setResult(ResponseEntity.ok(list));
            return deferred;
        }

        // register for async notification
        var userId = currentUserService.getCurrentUser().getId();
        var innerDeferred = new DeferredResult<List<NotificationDto>>(55_000L);
        pollingRegistry.register(userId, innerDeferred);
        innerDeferred.onTimeout(() -> deferred.setResult(ResponseEntity.noContent().build()));
        innerDeferred.onCompletion(() -> {
            Object res = innerDeferred.getResult();
            if (res == null) {
                deferred.setResult(ResponseEntity.noContent().build());
            } else {
                @SuppressWarnings("unchecked")
                List<NotificationDto> casted = (List<NotificationDto>) res;
                deferred.setResult(ResponseEntity.ok(casted));
            }
        });

        // keep work off request thread
        ForkJoinPool.commonPool().execute(() -> {});

        return deferred;
    }
}
