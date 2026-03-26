package com.haru.api.userdevice.dto;

import com.haru.api.userdevice.domain.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "디바이스 토큰 등록 요청")
public record RegisterDeviceTokenRequest(
        @Schema(description = "APNs device token", example = "9d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f0d4f")
        @NotBlank
        @Size(max = 512)
        String deviceToken,

        @Schema(description = "디바이스 플랫폼", example = "IOS")
        @NotNull
        DevicePlatform platform,

        @Schema(description = "푸시 수신 허용 여부", example = "true")
        boolean pushEnabled
) {
}
