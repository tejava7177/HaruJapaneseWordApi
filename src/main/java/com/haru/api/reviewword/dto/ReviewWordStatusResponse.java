package com.haru.api.reviewword.dto;

public record ReviewWordStatusResponse(
        Long userId,
        Long wordId,
        boolean reviewed
) {
}
