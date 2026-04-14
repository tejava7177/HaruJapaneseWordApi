package com.haru.api.notebook.dto;

public record NotebookItemDeleteResponse(
        Long userId,
        Long notebookId,
        Long itemId,
        boolean deleted
) {
}
