package com.haru.api.buddy.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBuddyRequestRequest(
        @NotNull
        Long requesterId,
        @NotNull
        Long targetUserId
) {
}
