package com.haru.api.buddy.dto;

public record DeleteBuddyResponse(
        Long userId,
        Long buddyUserId,
        boolean deleted
) {
    public static DeleteBuddyResponse success(Long userId, Long buddyUserId) {
        return new DeleteBuddyResponse(userId, buddyUserId, true);
    }
}
