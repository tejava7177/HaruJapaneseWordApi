package com.haru.api.user.dto;

import com.haru.api.user.domain.User;
import com.haru.api.word.domain.WordLevel;

public record UpdateLearningLevelResponse(
        Long userId,
        String nickname,
        WordLevel learningLevel,
        String message
) {
    private static final String UPDATE_MESSAGE =
            "Learning level updated. It will be applied to newly generated daily words.";

    public static UpdateLearningLevelResponse from(User user) {
        return new UpdateLearningLevelResponse(
                user.getId(),
                user.getNickname(),
                user.getLearningLevel(),
                UPDATE_MESSAGE
        );
    }
}
