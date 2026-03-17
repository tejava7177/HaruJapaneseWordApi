package com.haru.api.user.dto;

import com.haru.api.user.domain.User;

public record UpdateProfileImageResponse(
        Long userId,
        String profileImageUrl
) {
    public static UpdateProfileImageResponse from(User user) {
        return new UpdateProfileImageResponse(user.getId(), user.getProfileImageUrl());
    }
}
