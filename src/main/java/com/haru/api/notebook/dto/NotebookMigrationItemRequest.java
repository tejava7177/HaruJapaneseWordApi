package com.haru.api.notebook.dto;

import jakarta.validation.constraints.NotBlank;

public record NotebookMigrationItemRequest(
        Long wordId,
        @NotBlank(message = "expression must not be blank")
        String expression,
        String reading,
        @NotBlank(message = "meaning must not be blank")
        String meaning,
        String memo
) {
}
