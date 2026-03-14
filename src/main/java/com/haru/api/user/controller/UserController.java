package com.haru.api.user.controller;

import com.haru.api.user.dto.UpdateLearningLevelRequest;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdateRandomMatchingRequest;
import com.haru.api.user.dto.UpdateRandomMatchingResponse;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/buddy-code")
    @Operation(summary = "현재 사용자 초대코드 조회")
    public UserBuddyCodeResponse getBuddyCode(@PathVariable Long userId) {
        return userService.getBuddyCode(userId);
    }

    @PatchMapping("/{userId}/learning-level")
    @Operation(summary = "학습 레벨 변경")
    public UpdateLearningLevelResponse updateLearningLevel(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateLearningLevelRequest request
    ) {
        return userService.updateLearningLevel(userId, request.learningLevel());
    }

    @PatchMapping("/{userId}/random-matching")
    @Operation(summary = "랜덤 매칭 노출 설정 변경")
    public UpdateRandomMatchingResponse updateRandomMatching(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRandomMatchingRequest request
    ) {
        return userService.updateRandomMatchingEnabled(userId, request.enabled());
    }
}
