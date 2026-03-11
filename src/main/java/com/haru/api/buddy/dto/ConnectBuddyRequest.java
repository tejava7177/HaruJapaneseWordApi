package com.haru.api.buddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConnectBuddyRequest(
        @NotNull Long userId,
        @NotBlank String buddyCode
) {
}
