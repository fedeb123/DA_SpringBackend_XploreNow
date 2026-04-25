package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityItineraryCreateRequest;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityItineraryDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivityItinerary;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityItineraryService {

    private final ActivityItineraryRepository activityItineraryRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public ActivityItineraryDto createItinerary(Long activityId, ActivityItineraryCreateRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        ActivityItinerary itinerary = ActivityItinerary.builder()
                .activity(activity)
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .orderIndex(request.orderIndex())
                .build();

        ActivityItinerary saved = activityItineraryRepository.save(itinerary);
        return toDto(saved);
    }

    @Transactional
    public ActivityItineraryDto updateItinerary(Long itineraryId, ActivityItineraryCreateRequest request) {
        ActivityItinerary itinerary = activityItineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinerary not found"));

        itinerary.setName(request.name());
        itinerary.setLatitude(request.latitude());
        itinerary.setLongitude(request.longitude());
        itinerary.setOrderIndex(request.orderIndex());

        ActivityItinerary updated = activityItineraryRepository.save(itinerary);
        return toDto(updated);
    }

    @Transactional
    public void deleteItinerary(Long itineraryId) {
        if (!activityItineraryRepository.existsById(itineraryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinerary not found");
        }
        activityItineraryRepository.deleteById(itineraryId);
    }

    @Transactional(readOnly = true)
    public List<ActivityItineraryDto> getActivityItineraries(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found");
        }
        return activityItineraryRepository.findByActivityIdOrderByOrderIndex(activityId).stream()
                .map(this::toDto)
                .toList();
    }

    private ActivityItineraryDto toDto(ActivityItinerary itinerary) {
        return new ActivityItineraryDto(
                itinerary.getId(),
                itinerary.getName(),
                itinerary.getLatitude(),
                itinerary.getLongitude(),
                itinerary.getOrderIndex()
        );
    }
}
