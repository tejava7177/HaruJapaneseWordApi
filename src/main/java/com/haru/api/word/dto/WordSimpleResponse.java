package com.haru.api.word.dto;

import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;

public record WordSimpleResponse(
        Long id,
        String expression,
        String reading,
        WordLevel level
) {
    public static WordSimpleResponse from(Word word) {
        return new WordSimpleResponse(
                word.getId(),
                word.getExpression(),
                word.getReading(),
                word.getLevel()
        );
    }
}
