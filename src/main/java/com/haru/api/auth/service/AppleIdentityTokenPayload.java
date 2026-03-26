package com.haru.api.auth.service;

public record AppleIdentityTokenPayload(
        String subject,
        String email
) {
}
