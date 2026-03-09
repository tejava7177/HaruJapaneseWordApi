package com.haru.api.tsuntsun.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerTsunTsunRequest(
        @NotNull Long tsuntsunId,
        @NotNull Long meaningId
) {
}
