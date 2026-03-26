package com.haru.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Schema(description = "수정할 닉네임. null이면 기존 값 유지", example = "심주흔")
        @Size(max = 30, message = "must be at most 30 characters")
        String nickname,

        @Schema(description = "한 줄 소개. 빈 문자열이면 null로 저장", example = "매일 한 문장씩 일본어 연습 중")
        @Size(max = 160, message = "must be at most 160 characters")
        String bio,

        @Schema(description = "인스타그램 아이디. 빈 문자열이면 null로 저장", example = "@minsung_jp")
        @Size(max = 30, message = "must be at most 30 characters")
        String instagramId
) {
}
