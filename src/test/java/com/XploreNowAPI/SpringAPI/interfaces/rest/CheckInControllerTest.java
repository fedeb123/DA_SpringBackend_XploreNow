package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInCodeResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanResponseDto;
import com.XploreNowAPI.SpringAPI.application.service.CheckInService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.CheckInStatus;
import com.XploreNowAPI.SpringAPI.infrastructure.security.AppUserDetailsService;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtAuthenticationFilter;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@MockitoBean(types = JwtAuthenticationFilter.class)
@MockitoBean(types = JwtService.class)
@MockitoBean(types = AppUserDetailsService.class)
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService checkInService;

    @Test
    void getCheckInCode_ReturnsQrContent() throws Exception {
        when(checkInService.generateCheckInCode(5L)).thenReturn(
                new CheckInCodeResponseDto(5L, "qr-content", LocalDateTime.of(2026, 5, 10, 13, 0))
        );

        mockMvc.perform(get("/api/v1/schedules/{scheduleId}/checkin-code", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(5))
                .andExpect(jsonPath("$.qrContent").value("qr-content"));
    }

    @Test
    void scan_ReturnsConfirmation() throws Exception {
        when(checkInService.scan(org.mockito.ArgumentMatchers.any())).thenReturn(
                new CheckInScanResponseDto(CheckInStatus.CONFIRMED, 50L, "Free Tour Centro Historico", LocalDateTime.of(2026, 5, 10, 10, 2, 15), "Asistencia confirmada")
        );

        mockMvc.perform(post("/api/v1/checkin/scan")
                        .contentType("application/json")
                        .content("{\"reservationId\":50,\"qrContent\":\"qr-content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.message").value("Asistencia confirmada"));
    }
}