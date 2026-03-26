package com.haru.api.userdevice.dto;

import com.haru.api.userdevice.domain.DevicePlatform;
import com.haru.api.userdevice.domain.UserDeviceToken;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 토큰 등록 결과")
public record RegisterDeviceTokenResponse(
        Long userId,
        String deviceToken,
        DevicePlatform platform,
        boolean pushEnabled,
        boolean registered
) {

    public static RegisterDeviceTokenResponse from(UserDeviceToken userDeviceToken) {
        return new RegisterDeviceTokenResponse(
                userDeviceToken.getUser().getId(),
                userDeviceToken.getDeviceToken(),
                userDeviceToken.getPlatform(),
                userDeviceToken.isPushEnabled(),
                true
        );
    }
}
