package com.haru.api.user.dto;

import com.haru.api.word.domain.WordLevel;
import jakarta.validation.constraints.NotNull;

public record UpdateLearningLevelRequest(
        @NotNull WordLevel learningLevel
) {
}
