package com.XploreNowAPI.SpringAPI.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.ReservationEvent;

public interface ReservationEventRepository extends JpaRepository<ReservationEvent, Long> {
}
