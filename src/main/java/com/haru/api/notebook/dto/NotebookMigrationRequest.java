package com.haru.api.notebook.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record NotebookMigrationRequest(
        @NotNull(message = "notebooks must not be null")
        List<@Valid NotebookMigrationNotebookRequest> notebooks
) {
}
