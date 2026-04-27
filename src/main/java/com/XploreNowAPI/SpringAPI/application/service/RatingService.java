package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.rating.CreateRatingRequest;
import com.XploreNowAPI.SpringAPI.application.dto.rating.PendingRatingDto;
import com.XploreNowAPI.SpringAPI.application.dto.rating.RatingResponseDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Rating;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.RatingRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public RatingResponseDto createRating(CreateRatingRequest request) {
        AppUser user = currentUserService.getCurrentUser();

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to the authenticated user");
        }

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed reservations can be rated");
        }

        LocalDateTime completedAt = reservation.getSchedule().getEndDateTime();
        if (completedAt.plusHours(48).isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating window expired");
        }

        if (ratingRepository.existsByReservationId(reservation.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation already rated");
        }

        Rating rating = Rating.builder()
                .user(user)
                .reservation(reservation)
                .activityStars(request.activityStars())
                .guideStars(request.guideStars())
                .comment(request.comment() == null ? null : request.comment().trim())
                .build();

        Rating saved = ratingRepository.save(rating);

        return new RatingResponseDto(
                saved.getId(),
                reservation.getId(),
                saved.getActivityStars(),
                saved.getGuideStars(),
                saved.getComment(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<PendingRatingDto> getPendingRatings() {
        AppUser user = currentUserService.getCurrentUser();

        return reservationRepository.findByUserIdAndStatus(user.getId(), ReservationStatus.COMPLETED)
                .stream()
                .filter(reservation -> !ratingRepository.existsByReservationId(reservation.getId()))
                .filter(reservation -> reservation.getSchedule().getEndDateTime().plusHours(48).isAfter(LocalDateTime.now()))
                .map(reservation -> new PendingRatingDto(
                        reservation.getId(),
                        reservation.getSchedule().getActivity().getName(),
                        reservation.getSchedule().getEndDateTime(),
                        reservation.getSchedule().getEndDateTime().plusHours(48)
                ))
                .toList();
    }
}
