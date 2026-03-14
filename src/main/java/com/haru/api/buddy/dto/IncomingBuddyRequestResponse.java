package com.haru.api.buddy.dto;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.word.domain.WordLevel;

public record IncomingBuddyRequestResponse(
        Long requestId,
        Long requesterId,
        String nickname,
        WordLevel learningLevel,
        String profileImageUrl,
        String instagramId,
        String bio,
        String lastSeenText,
        BuddyRequestStatus status
) {
    public static IncomingBuddyRequestResponse from(BuddyRequest buddyRequest) {
        com.haru.api.user.domain.User requester = buddyRequest.getRequester();
        return new IncomingBuddyRequestResponse(
                buddyRequest.getId(),
                requester.getId(),
                requester.getNickname(),
                requester.getLearningLevel(),
                requester.getProfileImageUrl(),
                requester.getInstagramId(),
                requester.getBio(),
                BuddyProfileTextFormatter.lastSeenText(requester),
                buddyRequest.getStatus()
        );
    }
}
