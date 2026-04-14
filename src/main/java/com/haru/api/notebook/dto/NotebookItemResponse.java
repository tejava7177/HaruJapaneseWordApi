package com.haru.api.notebook.dto;

import com.haru.api.notebook.domain.NotebookItem;
import com.haru.api.notebook.domain.NotebookItemType;
import java.time.LocalDateTime;

public record NotebookItemResponse(
        Long id,
        NotebookItemType itemType,
        Long wordId,
        String expression,
        String reading,
        String meaning,
        String memo,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static NotebookItemResponse from(NotebookItem item) {
        return new NotebookItemResponse(
                item.getId(),
                item.getItemType(),
                item.getWord() != null ? item.getWord().getId() : null,
                item.getExpression(),
                item.getReading(),
                item.getMeaning(),
                item.getMemo(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
