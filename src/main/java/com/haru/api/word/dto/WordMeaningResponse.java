package com.haru.api.word.dto;

import com.haru.api.word.domain.Meaning;

public record WordMeaningResponse(
        Long id,
        String text,
        Integer ord
) {
    public static WordMeaningResponse from(Meaning meaning) {
        return new WordMeaningResponse(meaning.getId(), meaning.getText(), meaning.getOrd());
    }
}
