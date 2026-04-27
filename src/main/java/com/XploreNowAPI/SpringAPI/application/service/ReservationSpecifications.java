package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> historyFilter(
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            Long destinationId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.equal(root.get("status"), ReservationStatus.COMPLETED));

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("schedule").get("startDateTime"),
                        fromDate.atStartOfDay()
                ));
            }

            if (toDate != null) {
                LocalDateTime endOfDay = toDate.atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("schedule").get("startDateTime"),
                        endOfDay
                ));
            }

            if (destinationId != null) {
                predicates.add(cb.equal(
                        root.get("schedule").get("activity").get("destination").get("id"),
                        destinationId
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
