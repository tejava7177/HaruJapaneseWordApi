package com.haru.api.user.dto;

import com.haru.api.user.domain.User;

public record UpdatePetalNotificationsResponse(
        Long userId,
        boolean petalNotificationsEnabled
) {
    public static UpdatePetalNotificationsResponse from(User user) {
        return new UpdatePetalNotificationsResponse(user.getId(), user.isPetalNotificationsEnabled());
    }
}
