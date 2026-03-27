package com.haru.api.buddy.dto;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyStatus;
import java.time.LocalDateTime;

public record BuddyResponse(
        Long id,
        Long userId,
        Long buddyUserId,
        String buddyNickname,
        BuddyStatus status,
        Long tikiTakaCount,
        LocalDateTime lastActiveAt,
        boolean hasUnreadPetal,
        LocalDateTime lastReceivedAt,
        LocalDateTime lastInteractionAt
) {
    public static BuddyResponse from(
            Buddy buddy,
            long tikiTakaCount,
            LocalDateTime lastActiveAt,
            boolean hasUnreadPetal,
            LocalDateTime lastReceivedAt,
            LocalDateTime lastInteractionAt
    ) {
        return new BuddyResponse(
                buddy.getId(),
                buddy.getUser().getId(),
                buddy.getBuddyUser().getId(),
                buddy.getBuddyUser().getNickname(),
                buddy.getStatus(),
                tikiTakaCount,
                lastActiveAt,
                hasUnreadPetal,
                lastReceivedAt,
                lastInteractionAt
        );
    }
}
