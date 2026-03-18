package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityFilterRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.UserPreference;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ActivitySpecifications {

    private ActivitySpecifications() {
    }

    public static Specification<Activity> byFilter(ActivityFilterRequest filter) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.destinationId() != null) {
                predicates.add(cb.equal(root.get("destination").get("id"), filter.destinationId()));
            }

            if (filter.category() != null) {
                predicates.add(cb.equal(root.get("category"), filter.category()));
            }

            boolean dateFilter = filter.date() != null;
            boolean minPriceFilter = filter.minPrice() != null;
            boolean maxPriceFilter = filter.maxPrice() != null;
            if (dateFilter || minPriceFilter || maxPriceFilter) {
                Join<Object, Object> scheduleJoin = root.join("schedules", JoinType.INNER);
                predicates.add(cb.greaterThanOrEqualTo(scheduleJoin.get("startDateTime"), LocalDateTime.now()));

                if (dateFilter) {
                    LocalDateTime startOfDay = filter.date().atStartOfDay();
                    LocalDateTime endOfDay = filter.date().atTime(23, 59, 59);
                    predicates.add(cb.between(scheduleJoin.get("startDateTime"), startOfDay, endOfDay));
                }

                BigDecimal minPrice = filter.minPrice();
                BigDecimal maxPrice = filter.maxPrice();
                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(scheduleJoin.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(scheduleJoin.get("price"), maxPrice));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Activity> featuredByPreferences(List<UserPreference> preferences) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.isTrue(root.get("highlighted")));

            if (preferences == null || preferences.isEmpty()) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            List<Predicate> preferencePredicates = new ArrayList<>();
            for (UserPreference preference : preferences) {
                List<Predicate> current = new ArrayList<>();
                if (preference.getPreferredCategory() != null) {
                    current.add(cb.equal(root.get("category"), preference.getPreferredCategory()));
                }
                if (preference.getDestination() != null) {
                    current.add(cb.equal(root.get("destination").get("id"), preference.getDestination().getId()));
                }

                if (!current.isEmpty()) {
                    preferencePredicates.add(cb.and(current.toArray(new Predicate[0])));
                }
            }

            if (!preferencePredicates.isEmpty()) {
                predicates.add(cb.or(preferencePredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
