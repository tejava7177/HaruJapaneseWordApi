package com.haru.api.notebook.dto;

import com.haru.api.notebook.domain.Notebook;
import java.time.LocalDateTime;
import java.util.List;

public record NotebookResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<NotebookItemResponse> items
) {

    public static NotebookResponse from(Notebook notebook) {
        return new NotebookResponse(
                notebook.getId(),
                notebook.getTitle(),
                notebook.getDescription(),
                notebook.getCreatedAt(),
                notebook.getUpdatedAt(),
                notebook.getItems().stream()
                        .map(NotebookItemResponse::from)
                        .toList()
        );
    }
}
