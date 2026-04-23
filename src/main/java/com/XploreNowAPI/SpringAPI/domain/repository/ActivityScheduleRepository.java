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

    @Query("""
            select s from ActivitySchedule s
            where s.activity.id = :activityId
              and s.startDateTime >= :from
              and (:to is null or s.startDateTime <= :to)
              and s.totalSpots > s.reservedSpots
            order by s.startDateTime asc
            """)
    List<ActivitySchedule> findAvailableSchedules(
            @Param("activityId") Long activityId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ActivitySchedule s where s.id = :scheduleId")
    Optional<ActivitySchedule> findByIdForUpdate(@Param("scheduleId") Long scheduleId);
}
