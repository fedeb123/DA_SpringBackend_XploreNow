package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.reservation.ReservationSummaryDto;
import com.XploreNowAPI.SpringAPI.application.service.ReservationService;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void getMyReservations_ReturnsPagedReservations() throws Exception {
        ReservationSummaryDto dto = new ReservationSummaryDto(
                10L,
                "Free Tour Centro Historico",
                "Buenos Aires",
                java.time.LocalDate.of(2026, 5, 10),
                "10:00",
                2,
                ReservationStatus.CONFIRMED,
                "XPLR-ABC123"
        );

        Page<ReservationSummaryDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(reservationService.getMyReservations(eq(ReservationStatus.CONFIRMED), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/reservations/my")
                        .queryParam("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reservationId").value(10))
                .andExpect(jsonPath("$.content[0].voucherCode").value("XPLR-ABC123"));
    }
}
