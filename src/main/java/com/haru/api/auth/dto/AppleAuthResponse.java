package com.haru.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haru.api.user.domain.User;
import com.haru.api.word.domain.WordLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Apple 로그인 인증 결과")
public record AppleAuthResponse(
        @Schema(description = "백엔드 사용자 ID", example = "12")
        Long userId,

        @Schema(description = "Apple 사용자 고유 식별자", example = "000123.abcdeffedcba.1234")
        String appleUserId,

        @Schema(description = "앱에서 사용하는 닉네임", example = "심주흔")
        String nickname,

        @Schema(description = "현재 학습 레벨", example = "N3")
        WordLevel learningLevel,

        @Schema(description = "저장된 인증 이메일", example = "juheun9912@naver.com")
        String email,

        @Schema(description = "저장된 표시 이름", example = "심주흔")
        String displayName,

        @Schema(description = "신규 생성 사용자 여부", example = "false")
        @JsonProperty("isNewUser")
        boolean isNewUser,

        @Schema(description = "향후 세션 토큰 확장을 위한 필드. 현재는 비어 있습니다.", nullable = true)
        String sessionToken
) {

    public static AppleAuthResponse from(User user, boolean isNewUser) {
        return new AppleAuthResponse(
                user.getId(),
                user.getAppleSubject(),
                user.getNickname(),
                user.getLearningLevel(),
                user.getAuthEmail(),
                user.getDisplayName(),
                isNewUser,
                null
        );
    }
}
