package com.haru.api.buddy.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBuddyRequest(
        @NotNull Long userId,
        @NotNull Long buddyUserId
) {
}
