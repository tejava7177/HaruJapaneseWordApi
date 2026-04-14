package com.haru.api.notebook.dto;

import jakarta.validation.constraints.NotBlank;

public record NotebookCreateRequest(
        @NotBlank(message = "title must not be blank")
        String title,
        String description
) {
}
