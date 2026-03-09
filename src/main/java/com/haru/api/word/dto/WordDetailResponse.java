package com.haru.api.word.dto;

import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import java.util.List;

public record WordDetailResponse(
        Long id,
        String expression,
        String reading,
        WordLevel level,
        List<WordMeaningResponse> meanings
) {
    public static WordDetailResponse from(Word word) {
        List<WordMeaningResponse> meaningResponses = word.getMeanings().stream()
                .map(WordMeaningResponse::from)
                .toList();

        return new WordDetailResponse(
                word.getId(),
                word.getExpression(),
                word.getReading(),
                word.getLevel(),
                meaningResponses
        );
    }
}
