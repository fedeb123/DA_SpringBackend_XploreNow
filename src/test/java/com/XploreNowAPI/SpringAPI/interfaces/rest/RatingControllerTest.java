package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.rating.PendingRatingDto;
import com.XploreNowAPI.SpringAPI.application.service.RatingService;
import com.XploreNowAPI.SpringAPI.infrastructure.security.AppUserDetailsService;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtAuthenticationFilter;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void getPendingRatings_ReturnsList() throws Exception {
        PendingRatingDto dto = new PendingRatingDto(
                50L,
                "Tour Gastronomico Palermo",
                LocalDateTime.of(2026, 4, 21, 18, 0),
                LocalDateTime.of(2026, 4, 23, 18, 0)
        );

        when(ratingService.getPendingRatings()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/ratings/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(50))
                .andExpect(jsonPath("$[0].activityName").value("Tour Gastronomico Palermo"));
    }
}
