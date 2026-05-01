package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivitySummaryDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Destination;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityLanguage;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityItineraryRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.UserPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityQueryServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityScheduleRepository activityScheduleRepository;

    @Mock
    private ActivityItineraryRepository activityItineraryRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @InjectMocks
    private ActivityQueryService activityQueryService;

    @Test
    void getCatalog_SumsAvailableSpotsAcrossFutureSchedules() {
        Activity activity = Activity.builder()
                .id(1L)
                .name("Free Tour Centro")
                .destination(Destination.builder().name("Buenos Aires").build())
                .category(ActivityCategory.CULTURA)
                .durationMinutes(120)
                .basePrice(BigDecimal.ZERO)
                .currency("ARS")
                .language(ActivityLanguage.SPANISH)
                .meetingPoint("Plaza de Mayo")
                .cancellationPolicy("Cancelacion hasta 24h")
                .build();

        ActivitySchedule firstSchedule = schedule(activity, "0.00", 10, 6);
        ActivitySchedule secondSchedule = schedule(activity, "0.00", 12, 6);

        PageRequest pageable = PageRequest.of(0, 10);
        when(activityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(activity), pageable, 1));
        when(activityScheduleRepository.findByActivityIdAndStartDateTimeGreaterThanEqualOrderByStartDateTimeAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(firstSchedule, secondSchedule));
        when(activityScheduleRepository.findAvailableSchedulesFrom(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(firstSchedule, secondSchedule));

        Page<ActivitySummaryDto> result = activityQueryService.getCatalog(null, pageable, null);

        assertEquals(10, result.getContent().get(0).availableSpots());
    }

    private ActivitySchedule schedule(Activity activity, String price, int totalSpots, int reservedSpots) {
        return ActivitySchedule.builder()
                .activity(activity)
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal(price))
                .totalSpots(totalSpots)
                .reservedSpots(reservedSpots)
                .build();
    }
}
