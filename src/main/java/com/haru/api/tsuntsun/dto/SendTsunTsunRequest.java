package com.haru.api.tsuntsun.dto;

import jakarta.validation.constraints.NotNull;

public record SendTsunTsunRequest(
        @NotNull Long senderId,
        @NotNull Long receiverId,
        @NotNull Long dailyWordItemId
) {
}
