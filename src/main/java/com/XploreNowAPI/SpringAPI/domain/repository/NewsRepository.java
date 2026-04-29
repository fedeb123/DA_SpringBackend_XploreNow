package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByActiveTrueOrderByCreatedAtDesc();

    Optional<News> findByIdAndActiveTrue(Long id);
}
