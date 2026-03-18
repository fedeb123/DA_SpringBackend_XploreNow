package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.auth.AuthResponse;
import com.XploreNowAPI.SpringAPI.application.dto.auth.ClassicLoginRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpChallengeResponse;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.OtpVerifyRequest;
import com.XploreNowAPI.SpringAPI.application.dto.auth.RegisterRequest;
import com.XploreNowAPI.SpringAPI.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticacion y registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registro clasico", description = "Registra usuario con email y password y retorna JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerClassic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login clasico", description = "Autentica por email y password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody ClassicLoginRequest request) {
        return ResponseEntity.ok(authService.classicLogin(request));
    }

    @PostMapping("/otp/request")
    @Operation(summary = "Solicitar OTP", description = "Genera y envia OTP de 6 digitos por email")
    public ResponseEntity<OtpChallengeResponse> requestOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    @PostMapping("/otp/resend")
    @Operation(summary = "Reenviar OTP", description = "Invalida OTP pendiente previo y envia uno nuevo")
    public ResponseEntity<OtpChallengeResponse> resendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verificar OTP", description = "Valida codigo OTP y retorna sesion JWT")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}
