package com.haru.api.buddy.dto;

import com.haru.api.user.domain.User;
import com.haru.api.word.domain.WordLevel;

public record SeededDevUserResponse(
        Long id,
        String nickname,
        WordLevel learningLevel,
        String bio,
        String instagramId,
        String buddyCode,
        boolean randomMatchingEnabled
) {
    public static SeededDevUserResponse from(User user) {
        return new SeededDevUserResponse(
                user.getId(),
                user.getNickname(),
                user.getLearningLevel(),
                user.getBio(),
                user.getInstagramId(),
                user.getBuddyCode(),
                user.isRandomMatchingEnabled()
        );
    }
}
