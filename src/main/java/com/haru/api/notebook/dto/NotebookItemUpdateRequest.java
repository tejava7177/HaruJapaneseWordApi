package com.haru.api.notebook.dto;

import com.haru.api.notebook.domain.NotebookItemType;

public record NotebookItemUpdateRequest(
        NotebookItemType itemType,
        Long wordId,
        String expression,
        String reading,
        String meaning,
        String memo,
        Integer sortOrder
) {
}
