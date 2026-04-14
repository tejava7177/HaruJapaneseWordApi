package com.haru.api.notebook.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record NotebookMigrationNotebookRequest(
        @NotBlank(message = "title must not be blank")
        String title,
        String description,
        List<@Valid NotebookMigrationItemRequest> items
) {
}
