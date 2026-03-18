package com.XploreNowAPI.SpringAPI.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClassicLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
