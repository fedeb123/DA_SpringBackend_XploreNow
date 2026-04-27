package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.history.HistoryItemDto;
import com.XploreNowAPI.SpringAPI.application.service.HistoryService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoryService historyService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void getHistory_ReturnsPagedHistory() throws Exception {
        HistoryItemDto item = new HistoryItemDto(
                50L,
                "Tour Gastronomico Palermo",
                "Buenos Aires",
                LocalDate.of(2026, 3, 15),
                "Carlos Lopez",
                180,
                5,
                true
        );

        Page<HistoryItemDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(historyService.getHistory(isNull(), isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reservationId").value(50))
                .andExpect(jsonPath("$.content[0].hasRating").value(true));
    }
}
