package com.haru.api.dailyword.dto;

import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.word.domain.WordLevel;

public record DailyWordItemResponse(
        Long dailyWordItemId,
        Long wordId,
        String expression,
        String reading,
        WordLevel level,
        Integer orderIndex
) {
    public static DailyWordItemResponse from(DailyWordItem item) {
        return new DailyWordItemResponse(
                item.getId(),
                item.getWord().getId(),
                item.getWord().getExpression(),
                item.getWord().getReading(),
                item.getWord().getLevel(),
                item.getOrderIndex()
        );
    }
}
