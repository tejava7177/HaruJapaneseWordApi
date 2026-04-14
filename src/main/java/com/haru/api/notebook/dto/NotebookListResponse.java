package com.haru.api.notebook.dto;

import java.util.List;

public record NotebookListResponse(
        Long userId,
        List<NotebookResponse> notebooks
) {
}
