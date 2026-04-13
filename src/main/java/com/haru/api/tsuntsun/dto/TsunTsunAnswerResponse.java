package com.haru.api.tsuntsun.dto;

import com.haru.api.tsuntsun.domain.TsunTsunQuizType;

public record TsunTsunAnswerResponse(
        Long tsuntsunId,
        TsunTsunQuizType type,
        boolean correct,
        Long selectedChoiceId,
        Long correctChoiceId,
        String correctText,
        long pairProgressCount,
        long pairProgressGoal,
        boolean pairCompletedToday
) {
}
