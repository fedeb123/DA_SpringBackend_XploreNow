package com.XploreNowAPI.SpringAPI.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InitiateEmailChangeRequest(
        @NotBlank @Email String newEmail
) {}