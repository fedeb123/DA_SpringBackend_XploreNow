package com.XploreNowAPI.SpringAPI.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    List<UserPreference> findByUserId(Long userId);

    List<UserPreference> findByUserIdAndTravelPreferenceTypeIsNotNull(Long userId);

    void deleteByUserIdAndTravelPreferenceTypeIsNotNull(Long userId);
}
