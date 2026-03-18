package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.GuideProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideProfileRepository extends JpaRepository<GuideProfile, Long> {
}
