package com.haru.api.user.dto;

import com.haru.api.user.domain.User;

public record UserBuddyCodeResponse(
        Long userId,
        String buddyCode
) {
    public static UserBuddyCodeResponse from(User user) {
        return new UserBuddyCodeResponse(user.getId(), user.getBuddyCode());
    }
}
