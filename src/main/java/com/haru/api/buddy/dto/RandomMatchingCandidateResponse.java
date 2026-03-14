package com.haru.api.buddy.dto;

import com.haru.api.user.domain.User;
import com.haru.api.word.domain.WordLevel;

public record RandomMatchingCandidateResponse(
        Long userId,
        String nickname,
        WordLevel learningLevel,
        String profileImageUrl,
        String instagramId,
        String bio,
        String lastSeenText
) {
    public static RandomMatchingCandidateResponse from(User user) {
        return new RandomMatchingCandidateResponse(
                user.getId(),
                user.getNickname(),
                user.getLearningLevel(),
                user.getProfileImageUrl(),
                user.getInstagramId(),
                user.getBio(),
                BuddyProfileTextFormatter.lastSeenText(user)
        );
    }
}
