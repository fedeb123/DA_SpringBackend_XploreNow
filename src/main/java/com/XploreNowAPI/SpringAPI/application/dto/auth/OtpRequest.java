package com.XploreNowAPI.SpringAPI.application.dto.auth;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpRequest(
        @NotBlank @Email String email,
        @NotNull OtpPurpose purpose
) {
}
