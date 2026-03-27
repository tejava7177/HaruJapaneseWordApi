package com.haru.api.tsuntsun.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TsunTsunTodayResponse(
        Long userId,
        Long buddyId,
        LocalDate targetDate,
        long progressCount,
        long progressGoal,
        long sentCount,
        long receivedCount,
        boolean hasUnreadPetal,
        LocalDateTime lastReceivedAt,
        LocalDateTime lastInteractionAt,
        boolean pairCompletedToday,
        List<TsunTsunTodayItemResponse> items
) {
}
