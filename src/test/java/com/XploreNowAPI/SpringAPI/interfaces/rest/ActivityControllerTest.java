package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivityFilterOptionsDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.ActivitySummaryDto;
import com.XploreNowAPI.SpringAPI.application.dto.activity.DestinationOptionDto;
import com.XploreNowAPI.SpringAPI.application.service.ActivityCommandService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@MockitoBean(types = JwtAuthenticationFilter.class)
@MockitoBean(types = JwtService.class)
@MockitoBean(types = AppUserDetailsService.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityQueryService activityQueryService;

    @MockitoBean
    private ActivityCommandService activityCommandService;

    @Test
    void getFilterOptions_ReturnsDestinationsAndCategories() throws Exception {
        ActivityFilterOptionsDto options = new ActivityFilterOptionsDto(
                List.of(new DestinationOptionDto(1L, "Buenos Aires")),
                List.of(ActivityCategory.CULTURA, ActivityCategory.AVENTURA)
        );

        when(activityQueryService.getFilterOptions()).thenReturn(options);

        mockMvc.perform(get("/api/v1/activities/filter-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinations[0].destinationId").value(1))
                .andExpect(jsonPath("$.destinations[0].name").value("Buenos Aires"))
                .andExpect(jsonPath("$.categories[0]").value("CULTURA"));
    }

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
                10,
                false // <-- nuevo campo
        );

        Page<ActivitySummaryDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        when(activityQueryService.getCatalog(any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/activities")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].activityId").value(1))
                .andExpect(jsonPath("$.content[0].category").value("CULTURA"))
                .andExpect(jsonPath("$.content[0].featured").value(false));
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
                -34.604722,
                -58.371111,
                "Guia local",
                "Cancelacion hasta 24h",
                BigDecimal.ZERO,
                "ARS",
                20,
                List.of("https://img.test/10-1.jpg", "https://img.test/10-2.jpg"),
                Collections.emptyList()
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
                8,
                true // <-- nuevo campo
        );

        Page<ActivitySummaryDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        when(activityQueryService.getFeaturedForUser(eq(5L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/activities/featured")
                        .queryParam("userId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].activityId").value(3))
                .andExpect(jsonPath("$.content[0].category").value("AVENTURA"))
                .andExpect(jsonPath("$.content[0].featured").value(true));
    }
}
