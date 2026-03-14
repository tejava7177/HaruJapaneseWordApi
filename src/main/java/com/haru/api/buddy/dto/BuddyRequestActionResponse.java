package com.haru.api.buddy.dto;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;

public record BuddyRequestActionResponse(
        Long requestId,
        BuddyRequestStatus status
) {
    public static BuddyRequestActionResponse from(BuddyRequest buddyRequest) {
        return new BuddyRequestActionResponse(buddyRequest.getId(), buddyRequest.getStatus());
    }
}
