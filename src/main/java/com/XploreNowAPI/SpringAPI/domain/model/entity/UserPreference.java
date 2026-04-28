package com.XploreNowAPI.SpringAPI.domain.model.entity;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.TravelPreferenceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"user", "destination"})
@ToString(callSuper = true, exclude = {"user", "destination"})
@Entity
@Table(name = "user_preferences")
public class UserPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_category", length = 50)
    private ActivityCategory preferredCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_destination_id")
    private Destination destination;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_preference_type", length = 50)
    private TravelPreferenceType travelPreferenceType;
}
