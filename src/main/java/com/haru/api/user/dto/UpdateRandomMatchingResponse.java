package com.haru.api.user.dto;

import com.haru.api.user.domain.User;

public record UpdateRandomMatchingResponse(
        Long userId,
        boolean randomMatchingEnabled
) {
    public static UpdateRandomMatchingResponse from(User user) {
        return new UpdateRandomMatchingResponse(user.getId(), user.isRandomMatchingEnabled());
    }
}
