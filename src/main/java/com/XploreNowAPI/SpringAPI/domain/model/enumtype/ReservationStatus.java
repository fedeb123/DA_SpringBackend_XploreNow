package com.XploreNowAPI.SpringAPI.domain.model.enumtype;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED //TODO: hacer que se actualice el status a "completed" cuando ya haya pasado la act.
}
