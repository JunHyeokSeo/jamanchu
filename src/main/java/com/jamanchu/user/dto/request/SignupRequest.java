package com.jamanchu.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
        @NotNull @NotEmpty String email,
        @NotNull @NotEmpty String password
) {}