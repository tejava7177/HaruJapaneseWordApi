package com.haru.api.auth.controller;

import com.haru.api.auth.dto.AppleAuthRequest;
import com.haru.api.auth.dto.AppleAuthResponse;
import com.haru.api.auth.service.AppleAuthService;
import com.haru.api.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AppleAuthService appleAuthService;

    @PostMapping("/apple")
    @Operation(summary = "Apple 로그인 사용자 인증 및 백엔드 사용자 연결")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Apple 로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppleAuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": 12,
                                      "appleUserId": "000123.abcdeffedcba.1234",
                                      "nickname": "심주흔",
                                      "learningLevel": "N3",
                                      "email": "juheun9912@naver.com",
                                      "displayName": "심주흔",
                                      "isNewUser": false,
                                      "sessionToken": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 또는 Apple 토큰 파싱 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 만료된 Apple 토큰",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복 Apple 사용자 충돌",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "사용자 저장 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public AppleAuthResponse authenticateApple(@Valid @RequestBody AppleAuthRequest request) {
        return appleAuthService.authenticate(request);
    }
}
