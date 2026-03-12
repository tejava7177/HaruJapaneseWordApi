package com.haru.api.dailyword.dto;

import com.haru.api.word.domain.WordLevel;
import java.time.LocalDate;

public record DevDailyWordRegenerateResponse(
        Long userId,
        LocalDate targetDate,
        WordLevel level,
        int itemCount,
        String message
) {
    private static final String MESSAGE =
            "Today's daily words and related tsuntsun records were reset for development.";

    public static DevDailyWordRegenerateResponse from(DailyWordTodayResponse response) {
        return new DevDailyWordRegenerateResponse(
                response.userId(),
                response.targetDate(),
                response.level(),
                response.items().size(),
                MESSAGE
        );
    }
}
