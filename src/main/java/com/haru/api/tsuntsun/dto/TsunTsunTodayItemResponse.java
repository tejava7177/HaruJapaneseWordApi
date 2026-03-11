package com.haru.api.tsuntsun.dto;

public record TsunTsunTodayItemResponse(
        Long dailyWordItemId,
        Long wordId,
        TsunTsunDirection direction,
        TsunTsunTodayStatus status
) {
}
