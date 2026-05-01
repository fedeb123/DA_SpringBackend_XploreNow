package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.history.HistoryItemDto;
import com.XploreNowAPI.SpringAPI.application.service.HistoryService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@MockitoBean(types = JwtAuthenticationFilter.class)
@MockitoBean(types = JwtService.class)
@MockitoBean(types = AppUserDetailsService.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void getHistory_ReturnsPagedHistory() throws Exception {
        HistoryItemDto item = new HistoryItemDto(
                50L,
                "Tour Gastronomico Palermo",
                "Buenos Aires",
                LocalDate.of(2026, 3, 15),
                "Carlos Lopez",
                180,
                ReservationStatus.COMPLETED,
                5,
                true
        );

        Page<HistoryItemDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(historyService.getHistory(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/activity/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reservationId").value(50))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].hasRating").value(true));

        verify(historyService).getHistory(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
        );
    }

    @Test
    void getHistory_WithStatusFilter_ForwardsStatuses() throws Exception {
        Page<HistoryItemDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(historyService.getHistory(isNull(), isNull(), isNull(), eq(List.of(ReservationStatus.CANCELLED)), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/activity/history").param("status", "CANCELLED"))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_WithPendingStatusFilter_ForwardsPendingStatus() throws Exception {
        Page<HistoryItemDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(historyService.getHistory(isNull(), isNull(), isNull(), eq(List.of(ReservationStatus.PENDING)), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/activity/history").param("status", "PENDING"))
                .andExpect(status().isOk());
    }
}
