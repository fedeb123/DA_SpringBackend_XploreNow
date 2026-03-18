package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityFilterRequest;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivitySummaryDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivityImage;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityQueryService {

    private final ActivityRepository activityRepository;
    private final ActivityScheduleRepository activityScheduleRepository;
    private final AppUserRepository appUserRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional(readOnly = true)
    public Page<ActivitySummaryDto> getCatalog(ActivityFilterRequest filter, Pageable pageable) {
        Page<Activity> page = activityRepository.findAll(ActivitySpecifications.byFilter(filter), pageable);
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public ActivityDetailDto getActivityDetail(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        ActivitySchedule nextSchedule = getNextSchedule(activity.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No upcoming schedules"));

        List<String> gallery = activity.getImages().stream()
                .sorted(Comparator.comparing(ActivityImage::getDisplayOrder))
                .map(ActivityImage::getImageUrl)
                .toList();

        String guideName = activity.getGuide().getUser().getFirstName() + " " + activity.getGuide().getUser().getLastName();

        return new ActivityDetailDto(
                activity.getId(),
                activity.getName(),
                activity.getCategory(),
                activity.getShortDescription(),
                activity.getFullDescription(),
                activity.getDestination().getName(),
                guideName,
                activity.getDurationMinutes(),
                activity.getLanguage(),
                activity.getMeetingPoint(),
                activity.getInclusions(),
                activity.getCancellationPolicy(),
                nextSchedule.getPrice(),
                activity.getCurrency(),
                nextSchedule.getAvailableSpots(),
                gallery
        );
    }

    @Transactional(readOnly = true)
    public Page<ActivitySummaryDto> getFeaturedForUser(Long userId, Pageable pageable) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserPreference> preferences = userPreferenceRepository.findByUserId(user.getId());
        Page<Activity> featured = activityRepository.findAll(
                ActivitySpecifications.featuredByPreferences(preferences),
                pageable
        );

        return featured.map(this::toSummary);
    }

    private ActivitySummaryDto toSummary(Activity activity) {
        ActivitySchedule nextSchedule = getNextSchedule(activity.getId())
                .orElse(null);

        String image = activity.getImages().stream()
                .min(Comparator.comparing(ActivityImage::getDisplayOrder))
                .map(ActivityImage::getImageUrl)
                .orElse(null);

        return new ActivitySummaryDto(
                activity.getId(),
                image,
                activity.getName(),
                activity.getDestination().getName(),
                activity.getCategory(),
                activity.getDurationMinutes(),
                nextSchedule != null ? nextSchedule.getPrice() : activity.getBasePrice(),
                nextSchedule != null ? nextSchedule.getAvailableSpots() : 0
        );
    }

    private Optional<ActivitySchedule> getNextSchedule(Long activityId) {
        List<ActivitySchedule> schedules = activityScheduleRepository
                .findByActivityIdAndStartDateTimeGreaterThanEqualOrderByStartDateTimeAsc(activityId, LocalDateTime.now());

        return schedules.stream().findFirst();
    }
}
