package com.XploreNowAPI.SpringAPI.application.dto.auth;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}") String code,
        @NotNull OtpPurpose purpose
) {
}
