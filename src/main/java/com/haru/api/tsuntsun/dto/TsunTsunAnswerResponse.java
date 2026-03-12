package com.haru.api.tsuntsun.dto;

import com.haru.api.tsuntsun.domain.TsunTsunStatus;

public record TsunTsunAnswerResponse(
        Long tsuntsunId,
        boolean correct,
        Long selectedMeaningId,
        String selectedText,
        Long correctMeaningId,
        String correctText,
        TsunTsunStatus status
) {
}
