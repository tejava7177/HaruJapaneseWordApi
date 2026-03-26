package com.haru.api.userdevice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 토큰 비활성화 결과")
public record UnregisterDeviceTokenResponse(
        Long userId,
        String deviceToken,
        boolean pushEnabled,
        boolean unregistered
) {
}
