package com.haru.api.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRandomMatchingRequest(
        @NotNull
        Boolean enabled
) {
}
