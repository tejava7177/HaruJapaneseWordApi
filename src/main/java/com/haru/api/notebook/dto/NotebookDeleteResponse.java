package com.haru.api.notebook.dto;

public record NotebookDeleteResponse(
        Long userId,
        Long notebookId,
        boolean deleted
) {
}
