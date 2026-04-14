package com.haru.api.notebook.dto;

import com.haru.api.notebook.domain.NotebookItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotebookItemCreateRequest(
        @NotNull(message = "itemType must not be null")
        NotebookItemType itemType,
        Long wordId,
        @NotBlank(message = "expression must not be blank")
        String expression,
        String reading,
        @NotBlank(message = "meaning must not be blank")
        String meaning,
        String memo,
        Integer sortOrder
) {
}
