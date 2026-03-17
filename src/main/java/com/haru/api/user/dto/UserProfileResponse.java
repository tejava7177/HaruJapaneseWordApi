package com.haru.api.user.dto;

import com.haru.api.user.domain.User;
import com.haru.api.word.domain.WordLevel;

public record UserProfileResponse(
        Long userId,
        String nickname,
        WordLevel learningLevel,
        String bio,
        String instagramId,
        String buddyCode,
        boolean randomMatchingEnabled,
        String profileImageUrl
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getLearningLevel(),
                user.getBio(),
                user.getInstagramId(),
                user.getBuddyCode(),
                user.isRandomMatchingEnabled(),
                user.getProfileImageUrl()
        );
    }
}
