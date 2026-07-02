package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.reservation.ReservationSummaryDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.VoucherDto;
import com.XploreNowAPI.SpringAPI.application.service.ReservationService;
import com.XploreNowAPI.SpringAPI.application.service.CheckInService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@MockitoBean(types = JwtAuthenticationFilter.class)
@MockitoBean(types = JwtService.class)
@MockitoBean(types = AppUserDetailsService.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockitoBean
    private ReservationService reservationService;

        @MockitoBean
    private CheckInService checkInService;

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

    @Test
    void getVoucher_ReturnsVoucher() throws Exception {
        VoucherDto voucher = new VoucherDto(
                50L,
                "Free Tour Centro Historico",
                java.time.LocalDate.of(2026, 5, 10),
                "10:00",
                "Plaza de Mayo",
                "Maria Perez",
                2,
                ReservationStatus.CONFIRMED,
                true
        );

        when(checkInService.getVoucher(50L)).thenReturn(voucher);

        mockMvc.perform(get("/api/v1/reservations/{reservationId}/voucher", 50L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(50))
                .andExpect(jsonPath("$.checkedIn").value(true));
    }
}
