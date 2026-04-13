package com.haru.api.tsuntsun.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record AnswerTsunTsunRequest(
        @NotNull Long tsuntsunId,
        @JsonAlias("meaningId") @NotNull Long choiceId
) {
}
