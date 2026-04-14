package com.haru.api.notebook.dto;

public record NotebookMigrationResponse(
        Long userId,
        int migratedNotebookCount,
        int totalNotebookCount
) {
}
