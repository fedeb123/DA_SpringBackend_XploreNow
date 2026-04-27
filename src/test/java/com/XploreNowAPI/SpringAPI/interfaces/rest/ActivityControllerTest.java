package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivitySummaryDto;
import com.XploreNowAPI.SpringAPI.application.service.ActivityQueryService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityLanguage;
import com.XploreNowAPI.SpringAPI.infrastructure.security.AppUserDetailsService;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtAuthenticationFilter;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityQueryService activityQueryService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private AppUserDetailsService appUserDetailsService;

    @Test
    void getCatalog_ReturnsPagedContent() throws Exception {
        ActivitySummaryDto item = new ActivitySummaryDto(
                1L,
                "https://img.test/1.jpg",
                "Free Tour Centro",
                "Buenos Aires",
                ActivityCategory.CULTURA,
                120,
                BigDecimal.ZERO,
                10
        );

        Page<ActivitySummaryDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(activityQueryService.getCatalog(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/activities")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].activityId").value(1))
                .andExpect(jsonPath("$.content[0].category").value("CULTURA"));
    }

    @Test
    void getDetail_ReturnsActivityDetail() throws Exception {
        ActivityDetailDto detail = new ActivityDetailDto(
                10L,
                "Free Tour Centro Historico",
                ActivityCategory.CULTURA,
                "Descripcion corta",
                "Descripcion extensa",
                "Buenos Aires",
                "Lucia Fernandez",
                120,
                ActivityLanguage.SPANISH,
                "Plaza de Mayo",
                "Guia local",
                "Cancelacion hasta 24h",
                BigDecimal.ZERO,
                "ARS",
                20,
                List.of("https://img.test/10-1.jpg", "https://img.test/10-2.jpg")
        );

        when(activityQueryService.getActivityDetail(eq(10L))).thenReturn(detail);

        mockMvc.perform(get("/api/v1/activities/{activityId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId").value(10))
                .andExpect(jsonPath("$.language").value("SPANISH"));
    }

    @Test
    void getFeatured_ReturnsPagedRecommendedActivities() throws Exception {
        ActivitySummaryDto item = new ActivitySummaryDto(
                3L,
                "https://img.test/3.jpg",
                "Aventura Kayak",
                "Bariloche",
                ActivityCategory.AVENTURA,
                180,
                new BigDecimal("35000"),
                8
        );

        Page<ActivitySummaryDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(activityQueryService.getFeaturedForUser(eq(5L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/activities/featured")
                        .queryParam("userId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].activityId").value(3))
                .andExpect(jsonPath("$.content[0].category").value("AVENTURA"));
    }

}
