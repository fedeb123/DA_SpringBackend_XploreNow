package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Cuenta reservas del usuario cuyos estados estén
     * dentro del conjunto recibido.
     *
     * Ejemplo:
     * ACTIVE + CONFIRMED = actividades reservadas
     */
    Long countByUserAndStatusIn(
            AppUser user,
            Set<ReservationStatus> statuses
    );

    /**
     * Cuenta reservas del usuario con un estado puntual.
     *
     * Ejemplo:
     * COMPLETED = actividades realizadas
     */
    Long countByUserAndStatus(
            AppUser user,
            ReservationStatus status
    );
}