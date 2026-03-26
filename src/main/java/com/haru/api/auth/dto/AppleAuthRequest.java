package com.haru.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Apple 로그인 인증 요청")
public record AppleAuthRequest(
        @Schema(
                description = "Apple identity token(JWT). 제공되면 우선 사용됩니다.",
                example = "eyJraWQiOiJ...signature"
        )
        @Size(max = 4096)
        String identityToken,

        @Schema(
                description = "Apple 사용자 고유 식별자(sub). identityToken이 없을 때 fallback으로 사용됩니다.",
                example = "000123.abcdeffedcba.1234"
        )
        @Size(max = 255)
        String appleUserId,

        @Schema(description = "Apple에서 전달된 이메일. 첫 로그인 이후 비어 있을 수 있습니다.", example = "juheun9912@naver.com")
        @Size(max = 255)
        String email,

        @Schema(description = "Apple에서 전달된 표시 이름. 첫 로그인 이후 비어 있을 수 있습니다.", example = "심주흔")
        @Size(max = 255)
        String displayName
) {
}
