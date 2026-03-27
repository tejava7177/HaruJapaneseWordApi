package com.haru.api.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePetalNotificationsRequest(
        @NotNull
        Boolean enabled
) {
}
