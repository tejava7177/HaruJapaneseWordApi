package com.haru.api.tsuntsun.dto;

import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import java.time.LocalDate;
import java.util.List;

public record TsunTsunQuizResponse(
        Long tsuntsunId,
        Long senderId,
        Long receiverId,
        Long wordId,
        String expression,
        String reading,
        LocalDate targetDate,
        TsunTsunStatus status,
        List<QuizChoiceResponse> choices
) {
    public static TsunTsunQuizResponse from(TsunTsun tsuntsun, List<QuizChoiceResponse> choices) {
        return new TsunTsunQuizResponse(
                tsuntsun.getId(),
                tsuntsun.getSender().getId(),
                tsuntsun.getReceiver().getId(),
                tsuntsun.getWord().getId(),
                tsuntsun.getWord().getExpression(),
                tsuntsun.getWord().getReading(),
                tsuntsun.getTargetDate(),
                tsuntsun.getStatus(),
                choices
        );
    }
}
