package com.haru.api.tsuntsun.dto;

import java.time.LocalDate;
import java.util.List;

public record TsunTsunTodayResponse(
        Long userId,
        Long buddyId,
        LocalDate targetDate,
        long sentCount,
        long receivedCount,
        List<TsunTsunTodayItemResponse> items
) {
}
