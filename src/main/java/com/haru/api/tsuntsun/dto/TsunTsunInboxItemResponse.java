package com.haru.api.tsuntsun.dto;

import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunQuizType;
import java.time.LocalDate;
import java.util.List;

public record TsunTsunInboxItemResponse(
        Long tsuntsunId,
        Long senderId,
        String senderName,
        Long wordId,
        TsunTsunQuizType type,
        String expression,
        String reading,
        LocalDate targetDate,
        List<QuizChoiceResponse> choices
) {
    public static TsunTsunInboxItemResponse from(TsunTsun tsuntsun, List<QuizChoiceResponse> choices) {
        return new TsunTsunInboxItemResponse(
                tsuntsun.getId(),
                tsuntsun.getSender().getId(),
                tsuntsun.getSender().getNickname(),
                tsuntsun.getWord().getId(),
                tsuntsun.getQuizType(),
                tsuntsun.getWord().getExpression(),
                tsuntsun.getWord().getReading(),
                tsuntsun.getTargetDate(),
                choices
        );
    }
}
