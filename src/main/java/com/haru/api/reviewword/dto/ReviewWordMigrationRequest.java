package com.haru.api.reviewword.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReviewWordMigrationRequest(
        @NotNull
        List<Long> wordIds
) {
}
