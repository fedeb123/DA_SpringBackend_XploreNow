package com.XploreNowAPI.SpringAPI.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, ReservationStatus status);

    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    @Query("""
            select r from Reservation r
            join fetch r.schedule s
            where r.status = :status and s.endDateTime < :now
            """)
    List<Reservation> findByStatusWithEndedScheduleBefore(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query(value = "update reservations set status = :targetStatus where status = :sourceStatus", nativeQuery = true)
    int migrateStatuses(@Param("sourceStatus") String sourceStatus, @Param("targetStatus") String targetStatus);
}
package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.schedule s
            JOIN FETCH s.activity a
            JOIN FETCH a.destination d
            JOIN FETCH a.guide g
            JOIN FETCH g.user gu
            WHERE r.user.id = :userId
              AND r.status = :status
              AND (:destination IS NULL OR LOWER(d.name) = LOWER(:destination))
              AND (:startDateTime IS NULL OR s.startDateTime >= :startDateTime)
              AND (:endDateExclusive IS NULL OR s.startDateTime < :endDateExclusive)
            ORDER BY s.startDateTime DESC
            """)
    List<Reservation> findCompletedHistoryByUserId(
            @Param("userId") Long userId,
            @Param("status") ReservationStatus status,
            @Param("destination") String destination,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateExclusive") LocalDateTime endDateExclusive
    );

    Long countByUserAndStatusIn(
            AppUser user,
            Set<ReservationStatus> statuses
    );

    Long countByUserAndStatus(
            AppUser user,
            ReservationStatus status
    );

    void deleteAllByUser(AppUser user);
}
