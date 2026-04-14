package com.haru.api.notebook.dto;

public record NotebookUpdateRequest(
        String title,
        String description
) {
}
