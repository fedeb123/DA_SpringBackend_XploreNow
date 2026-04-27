package com.XploreNowAPI.SpringAPI.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByReservationId(Long reservationId);

    Optional<Rating> findByReservationId(Long reservationId);

    List<Rating> findByReservationIdIn(Collection<Long> reservationIds);
}
