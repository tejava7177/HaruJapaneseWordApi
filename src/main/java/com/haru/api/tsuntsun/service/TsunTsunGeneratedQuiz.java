package com.haru.api.tsuntsun.service;

import com.haru.api.tsuntsun.domain.TsunTsunQuizType;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import java.util.List;

public record TsunTsunGeneratedQuiz(
        TsunTsunQuizType type,
        List<QuizChoiceResponse> choices
) {
}
