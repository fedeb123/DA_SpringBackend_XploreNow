package com.XploreNowAPI.SpringAPI.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.CheckIn;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.CheckInStatus;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    boolean existsByReservationIdAndStatus(Long reservationId, CheckInStatus status);
}