package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByActiveTrueOrderByNameAsc();
}
