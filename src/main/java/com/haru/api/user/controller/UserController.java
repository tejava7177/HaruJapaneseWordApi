package com.haru.api.user.controller;

import com.haru.api.user.dto.ActivePingResponse;
import com.haru.api.user.dto.UpdateLearningLevelRequest;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdatePetalNotificationsRequest;
import com.haru.api.user.dto.UpdatePetalNotificationsResponse;
import com.haru.api.user.dto.UpdateProfileImageResponse;
import com.haru.api.user.dto.UpdateRandomMatchingRequest;
import com.haru.api.user.dto.UpdateRandomMatchingResponse;
import com.haru.api.user.dto.UpdateUserProfileRequest;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.dto.UserProfileResponse;
import com.haru.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 프로필 조회")
    public UserProfileResponse getUserProfile(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }

    @GetMapping("/{userId}/buddy-code")
    @Operation(summary = "현재 사용자 초대코드 조회")
    public UserBuddyCodeResponse getBuddyCode(@PathVariable Long userId) {
        return userService.getBuddyCode(userId);
    }

    @PostMapping("/{userId}/active-ping")
    @Operation(summary = "앱 foreground 진입 시 최근 접속 갱신")
    public ActivePingResponse pingActive(@PathVariable Long userId) {
        return userService.pingActive(userId);
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

    @PatchMapping("/{userId}/petal-notifications")
    @Operation(summary = "꽃잎 알림 설정 변경")
    public UpdatePetalNotificationsResponse updatePetalNotifications(
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePetalNotificationsRequest request
    ) {
        return userService.updatePetalNotificationsEnabled(userId, request.enabled());
    }

    @PatchMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 수정")
    public UserProfileResponse updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userService.updateUserProfile(userId, request.nickname(), request.bio(), request.instagramId());
    }

    @PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 사진 업로드")
    public UpdateProfileImageResponse uploadProfileImage(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        return userService.uploadProfileImage(userId, file);
    }
}
