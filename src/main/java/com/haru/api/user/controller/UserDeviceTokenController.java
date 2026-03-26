package com.haru.api.user.controller;

import com.haru.api.common.exception.ApiErrorResponse;
import com.haru.api.userdevice.dto.RegisterDeviceTokenRequest;
import com.haru.api.userdevice.dto.RegisterDeviceTokenResponse;
import com.haru.api.userdevice.dto.UnregisterDeviceTokenResponse;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/devices")
@RequiredArgsConstructor
@Tag(name = "User Devices")
public class UserDeviceTokenController {

    private final UserDeviceTokenService userDeviceTokenService;

    @PostMapping
    @Operation(summary = "iOS 디바이스 토큰 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디바이스 토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public RegisterDeviceTokenResponse registerDeviceToken(
            @PathVariable Long userId,
            @Valid @RequestBody RegisterDeviceTokenRequest request
    ) {
        return userDeviceTokenService.registerToken(userId, request);
    }

    @DeleteMapping("/{deviceToken:.+}")
    @Operation(summary = "디바이스 토큰 비활성화")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디바이스 토큰 비활성화 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UnregisterDeviceTokenResponse unregisterDeviceToken(
            @PathVariable Long userId,
            @PathVariable String deviceToken
    ) {
        return userDeviceTokenService.unregisterToken(userId, deviceToken);
    }
}
