package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivityItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityItineraryRepository extends JpaRepository<ActivityItinerary, Long> {
    @Query("SELECT ai FROM ActivityItinerary ai WHERE ai.activity.id = :activityId ORDER BY ai.orderIndex ASC")
    List<ActivityItinerary> findByActivityIdOrderByOrderIndex(@Param("activityId") Long activityId);
}
