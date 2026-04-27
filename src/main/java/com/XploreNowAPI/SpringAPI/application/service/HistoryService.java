package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.history.HistoryDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.history.HistoryItemDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Rating;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.RatingRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;
    private final RatingRepository ratingRepository;

    @Transactional(readOnly = true)
    public Page<HistoryItemDto> getHistory(
            LocalDate fromDate,
            LocalDate toDate,
            Long destinationId,
            Pageable pageable
    ) {
        AppUser user = currentUserService.getCurrentUser();

        Page<Reservation> historyPage = reservationRepository.findAll(
                ReservationSpecifications.historyFilter(user.getId(), fromDate, toDate, destinationId),
                pageable
        );

        Map<Long, Rating> ratingsByReservationId = ratingRepository
                .findByReservationIdIn(historyPage.getContent().stream().map(Reservation::getId).toList())
                .stream()
                .collect(Collectors.toMap(r -> r.getReservation().getId(), Function.identity()));

        return historyPage.map(reservation -> toHistoryItem(reservation, ratingsByReservationId.get(reservation.getId())));
    }

    @Transactional(readOnly = true)
    public HistoryDetailDto getHistoryDetail(Long reservationId) {
        AppUser user = currentUserService.getCurrentUser();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to the authenticated user");
        }

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is not completed");
        }

        Rating rating = ratingRepository.findByReservationId(reservationId).orElse(null);
        return toHistoryDetail(reservation, rating);
    }

    private HistoryItemDto toHistoryItem(Reservation reservation, Rating rating) {
        Activity activity = reservation.getSchedule().getActivity();
        String guideName = activity.getGuide().getUser().getFirstName() + " " + activity.getGuide().getUser().getLastName();

        return new HistoryItemDto(
                reservation.getId(),
                activity.getName(),
                activity.getDestination().getName(),
                reservation.getSchedule().getStartDateTime().toLocalDate(),
                guideName,
                activity.getDurationMinutes(),
                rating != null ? rating.getActivityStars() : null,
                rating != null
        );
    }

    private HistoryDetailDto toHistoryDetail(Reservation reservation, Rating rating) {
        Activity activity = reservation.getSchedule().getActivity();
        String guideName = activity.getGuide().getUser().getFirstName() + " " + activity.getGuide().getUser().getLastName();

        return new HistoryDetailDto(
                reservation.getId(),
                activity.getName(),
                activity.getDestination().getName(),
                reservation.getSchedule().getStartDateTime().toLocalDate(),
                guideName,
                activity.getDurationMinutes(),
                activity.getMeetingPoint(),
                activity.getCancellationPolicy(),
                rating != null ? rating.getActivityStars() : null,
                rating != null ? rating.getGuideStars() : null,
                rating != null ? rating.getComment() : null,
                rating != null
        );
    }
}
