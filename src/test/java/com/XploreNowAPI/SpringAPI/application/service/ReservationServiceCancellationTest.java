package com.XploreNowAPI.SpringAPI.application.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.XploreNowAPI.SpringAPI.application.dto.reservation.CancelActivityResponseDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.NotificationType;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityItineraryRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.CheckInRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationEventRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCancellationTest {

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ActivityScheduleRepository activityScheduleRepository;
    @Mock
    private ActivityItineraryRepository activityItineraryRepository;
    @Mock
    private ReservationEventRepository reservationEventRepository;
    @Mock
    private CheckInRepository checkInRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void cancelActivityReservationsCancelsConfirmedReservationsAndNotifiesUsers() {
        ActivitySchedule schedule = ActivitySchedule.builder()
                .id(10L)
                .reservedSpots(5)
                .activity(Activity.builder().name("Trekking").build())
                .build();

        AppUser userOne = AppUser.builder().id(1L).build();
        AppUser userTwo = AppUser.builder().id(2L).build();

        Reservation first = Reservation.builder()
                .id(100L)
                .user(userOne)
                .schedule(schedule)
                .seats(2)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation second = Reservation.builder()
                .id(101L)
                .user(userTwo)
                .schedule(schedule)
                .seats(3)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(activityScheduleRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(schedule));
        when(reservationRepository.findByScheduleIdAndStatus(10L, ReservationStatus.CONFIRMED)).thenReturn(List.of(first, second));

        CancelActivityResponseDto response = reservationService.cancelActivityReservations(10L);

        assertEquals(2, response.cancelledReservations());
        assertEquals(ReservationStatus.CANCELLED, first.getStatus());
        assertEquals(ReservationStatus.CANCELLED, second.getStatus());
        assertEquals(0, schedule.getReservedSpots());
        verify(notificationService, times(2)).createImmediate(any(), any(), eq(NotificationType.INFO), anyString());
    }
}
