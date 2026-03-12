package com.haru.api.tsuntsun.dto;

public record TsunTsunAnswerResponse(
        Long tsuntsunId,
        boolean correct,
        Long selectedMeaningId,
        Long correctMeaningId,
        String correctText
) {
}
