package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityFilterRequest;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ScheduleSummaryDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivitySummaryDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityItineraryDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivityImage;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityItineraryRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityQueryService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ActivityRepository activityRepository;
    private final ActivityScheduleRepository activityScheduleRepository;
    private final ActivityItineraryRepository activityItineraryRepository;
    private final AppUserRepository appUserRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional(readOnly = true)
    public Page<ActivitySummaryDto> getCatalog(ActivityFilterRequest filter, Pageable pageable, Long userId) {

        Page<Activity> page = activityRepository.findAll(
                ActivitySpecifications.byFilter(filter),
                pageable
        );

        Set<ActivityCategory> preferredCategories =
                (userId != null)
                        ? userPreferenceRepository.findByUserId(userId).stream()
                        .map(UserPreference::getPreferredCategory)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
                        : Set.of();

        List<ActivitySummaryDto> content = page.getContent().stream()
                .map(activity -> toSummary(activity, preferredCategories))
                .sorted(Comparator.comparing(ActivitySummaryDto::featured).reversed())
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
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

        List<ActivityItineraryDto> itineraries = activityItineraryRepository
                .findByActivityIdOrderByOrderIndex(activityId).stream()
                .map(it -> new ActivityItineraryDto(it.getId(), it.getName(), it.getLatitude(), it.getLongitude(), it.getOrderIndex()))
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
                activity.getMeetingPointLatitude(),
                activity.getMeetingPointLongitude(),
                activity.getInclusions(),
                activity.getCancellationPolicy(),
                nextSchedule.getPrice(),
                activity.getCurrency(),
                nextSchedule.getAvailableSpots(),
                gallery,
                itineraries
        );
    }

    @Transactional(readOnly = true)
    public Page<ActivitySummaryDto> getFeaturedForUser(Long userId, Pageable pageable) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserPreference> preferences = userPreferenceRepository.findByUserId(user.getId());

        Set<ActivityCategory> preferredCategories = preferences.stream()
                .map(UserPreference::getPreferredCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Page<Activity> featured = activityRepository.findAll(
                ActivitySpecifications.featuredByPreferences(preferences),
                pageable
        );

        return featured.map(activity -> toSummary(activity, preferredCategories));
    }

    @Transactional(readOnly = true)
    public List<ScheduleSummaryDto> getAvailableSchedules(Long activityId, LocalDate date) {
        if (!activityRepository.existsById(activityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found");
        }

        LocalDateTime now = LocalDateTime.now();
        List<ActivitySchedule> schedules;

        if (date == null) {
            schedules = activityScheduleRepository.findAvailableSchedulesFrom(activityId, now);
        } else {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            LocalDateTime from = dayStart.isAfter(now) ? dayStart : now;

            if (dayEnd.isBefore(from)) {
                return List.of();
            }

            schedules = activityScheduleRepository.findAvailableSchedulesBetween(activityId, from, dayEnd);
        }

        return schedules.stream()
                .map(schedule -> new ScheduleSummaryDto(
                        schedule.getId(),
                        schedule.getStartDateTime().toLocalDate(),
                        schedule.getStartDateTime().toLocalTime().format(TIME_FORMATTER),
                        schedule.getAvailableSpots(),
                        schedule.getTotalSpots()
                ))
                .toList();
    }

    private ActivitySummaryDto toSummary(Activity activity, Set<ActivityCategory> preferredCategories) {
        ActivitySchedule nextSchedule = getNextSchedule(activity.getId())
                .orElse(null);

        String image = activity.getImages().stream()
                .min(Comparator.comparing(ActivityImage::getDisplayOrder))
                .map(ActivityImage::getImageUrl)
                .orElse(null);

        Integer availableSpots = getTotalAvailableSpots(activity.getId());

        boolean isFeatured = preferredCategories != null
                && preferredCategories.contains(activity.getCategory());

        System.out.println(
                "Activity: " + activity.getName()
                        + " | category=" + activity.getCategory()
                        + " | preferred=" + preferredCategories
                        + " | match=" + preferredCategories.contains(activity.getCategory())
        );

        return new ActivitySummaryDto(
                activity.getId(),
                image,
                activity.getName(),
                activity.getDestination().getName(),
                activity.getCategory(),
                activity.getDurationMinutes(),
                nextSchedule != null ? nextSchedule.getPrice() : activity.getBasePrice(),
                availableSpots,
                isFeatured
        );
    }

    private Optional<ActivitySchedule> getNextSchedule(Long activityId) {
        List<ActivitySchedule> schedules = activityScheduleRepository
                .findByActivityIdAndStartDateTimeGreaterThanEqualOrderByStartDateTimeAsc(
                        activityId,
                        LocalDateTime.now()
                );

        return schedules.stream().findFirst();
    }

    private Integer getTotalAvailableSpots(Long activityId) {
        return activityScheduleRepository.findAvailableSchedulesFrom(activityId, LocalDateTime.now()).stream()
                .map(ActivitySchedule::getAvailableSpots)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
