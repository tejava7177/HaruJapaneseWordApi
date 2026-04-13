package com.haru.api.user.dto;

import java.time.LocalDateTime;

public record ActivePingResponse(
        Long userId,
        LocalDateTime lastActiveAt
) {
}
