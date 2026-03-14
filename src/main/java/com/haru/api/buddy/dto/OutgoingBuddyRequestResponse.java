package com.haru.api.buddy.dto;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.word.domain.WordLevel;

public record OutgoingBuddyRequestResponse(
        Long requestId,
        Long targetUserId,
        String nickname,
        WordLevel learningLevel,
        String profileImageUrl,
        String instagramId,
        String bio,
        String lastSeenText,
        BuddyRequestStatus status
) {
    public static OutgoingBuddyRequestResponse from(BuddyRequest buddyRequest) {
        com.haru.api.user.domain.User targetUser = buddyRequest.getTargetUser();
        return new OutgoingBuddyRequestResponse(
                buddyRequest.getId(),
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getLearningLevel(),
                targetUser.getProfileImageUrl(),
                targetUser.getInstagramId(),
                targetUser.getBio(),
                BuddyProfileTextFormatter.lastSeenText(targetUser),
                buddyRequest.getStatus()
        );
    }
}
