package com.haru.api.dailyword.dto;

import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.word.domain.WordLevel;
import java.time.LocalDate;
import java.util.List;

public record DailyWordTodayResponse(
        Long userId,
        LocalDate targetDate,
        WordLevel level,
        List<DailyWordItemResponse> items
) {
    public static DailyWordTodayResponse from(DailyWordSet dailyWordSet) {
        List<DailyWordItemResponse> itemResponses = dailyWordSet.getItems().stream()
                .map(DailyWordItemResponse::from)
                .toList();

        return new DailyWordTodayResponse(
                dailyWordSet.getUser().getId(),
                dailyWordSet.getTargetDate(),
                dailyWordSet.getLevel(),
                itemResponses
        );
    }
}
