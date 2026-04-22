package com.XploreNowAPI.SpringAPI.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, ReservationStatus status);

    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);
}
