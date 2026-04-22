package com.XploreNowAPI.SpringAPI.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;

import jakarta.persistence.LockModeType;

public interface ActivityScheduleRepository extends JpaRepository<ActivitySchedule, Long> {

    List<ActivitySchedule> findByActivityIdAndStartDateTimeGreaterThanEqualOrderByStartDateTimeAsc(
            Long activityId,
            LocalDateTime from
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ActivitySchedule s where s.id = :scheduleId")
    Optional<ActivitySchedule> findByIdForUpdate(@Param("scheduleId") Long scheduleId);
}
