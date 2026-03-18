package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.auth.AuthResponse;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.RegisterRequest;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Role;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.RoleType;
import com.XploreNowAPI.SpringAPI.domain.repository.AppUserRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.OtpVerificationRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.RoleRepository;
import com.XploreNowAPI.SpringAPI.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private OtpDeliveryService otpDeliveryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpirationMinutes", 10L);
        ReflectionTestUtils.setField(authService, "otpMaxAttempts", 5);
    }

    @Test
    void registerClassic_WhenEmailAlreadyExists_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest("existing@test.com", "Password123", "A", "B", null);
        when(userRepository.existsByEmailIgnoreCase("existing@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.registerClassic(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Email is already registered", ex.getReason());
    }

    @Test
    void requestOtp_WhenUserNotFound_ThrowsNotFound() {
        OtpRequest request = new OtpRequest("nouser@test.com", OtpPurpose.LOGIN);
        when(userRepository.findByEmailIgnoreCase("nouser@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.requestOtp(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    @Test
    void registerClassic_WhenSuccess_ReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("new@test.com", "Password123", "Ana", "Gomez", "+5491112345678");

        Role travelerRole = Role.builder().id(1L).name(RoleType.TRAVELER).build();
        AppUser saved = AppUser.builder()
                .id(10L)
                .email("new@test.com")
                .firstName("Ana")
                .lastName("Gomez")
                .enabled(true)
                .build();

        when(userRepository.existsByEmailIgnoreCase("new@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.TRAVELER)).thenReturn(Optional.of(travelerRole));
        when(passwordEncoder.encode("Password123")).thenReturn("ENCODED-PWD");
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(7200L);

        AuthResponse result = authService.registerClassic(request);

        assertEquals("jwt-token", result.token());
        assertEquals("Bearer", result.tokenType());
        assertEquals("new@test.com", result.email());
        assertEquals("Ana Gomez", result.fullName());
        verify(userRepository).save(any(AppUser.class));
    }
}
