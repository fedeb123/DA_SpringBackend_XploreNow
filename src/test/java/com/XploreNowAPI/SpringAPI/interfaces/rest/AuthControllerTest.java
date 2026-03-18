package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.auth.AuthResponse;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpChallengeResponse;
import com.XploreNowAPI.SpringAPI.application.service.AuthService;
import com.XploreNowAPI.SpringAPI.infrastructure.security.AppUserDetailsService;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtAuthenticationFilter;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void register_ReturnsCreatedAndAuthPayload() throws Exception {
        AuthResponse response = new AuthResponse("token-1", 7200L, "Bearer", "user@test.com", "Test User");
        when(authService.registerClassic(any())).thenReturn(response);

        String payload = """
                {
                  "email": "user@test.com",
                  "password": "Password123",
                  "firstName": "Test",
                  "lastName": "User",
                  "phone": "+5491112345678"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-1"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_ReturnsOkAndAuthPayload() throws Exception {
        AuthResponse response = new AuthResponse("token-2", 7200L, "Bearer", "user@test.com", "Test User");
        when(authService.classicLogin(any())).thenReturn(response);

        String payload = """
                {
                  "email": "user@test.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-2"));
    }

    @Test
    void requestOtp_ReturnsOkAndOtpChallengePayload() throws Exception {
        OtpChallengeResponse response = new OtpChallengeResponse("user@test.com", "LOGIN", 600L, "OTP sent successfully");
        when(authService.requestOtp(any())).thenReturn(response);

        String payload = """
                {
                  "email": "user@test.com",
                  "purpose": "LOGIN"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purpose").value("LOGIN"));
    }
}
