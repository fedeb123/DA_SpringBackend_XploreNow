package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.news.NewsDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.news.NewsSummaryDto;
import com.XploreNowAPI.SpringAPI.application.service.NewsCommandService;
import com.XploreNowAPI.SpringAPI.application.service.NewsQueryService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsQueryService newsQueryService;

    @MockBean
    private NewsCommandService newsCommandService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void getCatalog_ReturnsNewsList() throws Exception {
        NewsSummaryDto item = new NewsSummaryDto(
                1L,
                "Nueva temporada",
                "Short",
                "https://img.test/1.jpg",
                LocalDateTime.parse("2026-05-01T10:15:30")
        );
        when(newsQueryService.getCatalog()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].newsId").value(1))
                .andExpect(jsonPath("$[0].title").value("Nueva temporada"));
    }

    @Test
    void getDetail_ReturnsNewsDetail() throws Exception {
        NewsDetailDto detail = new NewsDetailDto(
                10L,
                "Nueva temporada",
                "Short",
                "Full description",
                "https://img.test/10.jpg",
                LocalDateTime.parse("2026-05-01T10:15:30"),
                LocalDateTime.parse("2026-05-01T11:00:00")
        );
        when(newsQueryService.getNewsDetail(eq(10L))).thenReturn(detail);

        mockMvc.perform(get("/api/v1/news/{newsId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsId").value(10))
                .andExpect(jsonPath("$.title").value("Nueva temporada"));
    }

    @Test
    void updateStatus_ReturnsNoContent() throws Exception {
        doNothing().when(newsCommandService).updateStatus(10L, false);

        mockMvc.perform(put("/api/v1/news/{newsId}/status", 10L)
                        .contentType("application/json")
                        .content("{\"active\":false}"))
                .andExpect(status().isNoContent());
    }
}
