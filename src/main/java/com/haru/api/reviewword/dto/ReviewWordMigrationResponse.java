package com.haru.api.reviewword.dto;

public record ReviewWordMigrationResponse(
        Long userId,
        int migratedCount,
        int totalWordIds
) {
}
