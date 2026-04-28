package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.activity.UpdateMeetingPointRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ActivityCommandService {

    private final ActivityRepository activityRepository;

    @Transactional
    public void updateMeetingPoint(Long activityId, UpdateMeetingPointRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        activity.setMeetingPoint(request.address());
        activity.setMeetingPointLatitude(request.latitude());
        activity.setMeetingPointLongitude(request.longitude());

        activityRepository.save(activity);
    }
}
