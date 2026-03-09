package com.haru.api.tsuntsun.dto;

import com.haru.api.tsuntsun.domain.TsunTsunStatus;

public record TsunTsunAnswerResponse(
        Long tsuntsunId,
        boolean correct,
        String selectedMeaning,
        Long correctMeaningId,
        String correctMeaning,
        TsunTsunStatus status
) {
}
