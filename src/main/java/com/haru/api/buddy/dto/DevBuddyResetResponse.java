package com.haru.api.buddy.dto;

public record DevBuddyResetResponse(String message) {
    public static DevBuddyResetResponse success() {
        return new DevBuddyResetResponse("Buddy, tsuntsun, and buddy_relationship data were reset for development.");
    }
}
