package com.haru.api.user.service;

import com.haru.api.user.domain.User;
import com.haru.api.user.dto.ActivePingResponse;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdatePetalNotificationsResponse;
import com.haru.api.user.dto.UpdateProfileImageResponse;
import com.haru.api.user.dto.UpdateRandomMatchingResponse;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.dto.UserProfileResponse;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final int MAX_NICKNAME_LENGTH = 30;
    private static final int MAX_BIO_LENGTH = 160;
    private static final int MAX_INSTAGRAM_ID_LENGTH = 30;

    private final UserRepository userRepository;
    private final ProfileImageStorageService profileImageStorageService;
    private final ActivityTrackingService activityTrackingService;

    @Transactional
    public UpdateLearningLevelResponse updateLearningLevel(Long userId, WordLevel learningLevel) {
        User user = findUserOrThrow(userId);

        user.changeLearningLevel(learningLevel);
        return UpdateLearningLevelResponse.from(user);
    }

    @Transactional
    public UpdateRandomMatchingResponse updateRandomMatchingEnabled(Long userId, boolean enabled) {
        User user = findUserOrThrow(userId);

        user.changeRandomMatchingEnabled(enabled);
        return UpdateRandomMatchingResponse.from(user);
    }

    @Transactional
    public UpdatePetalNotificationsResponse updatePetalNotificationsEnabled(Long userId, boolean enabled) {
        User user = findUserOrThrow(userId);

        user.changePetalNotificationsEnabled(enabled);
        return UpdatePetalNotificationsResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, String nickname, String bio, String instagramId) {
        User user = findUserOrThrow(userId);

        String normalizedNickname = normalizeNickname(nickname);
        String normalizedBio = normalizeOptionalText("bio", bio, MAX_BIO_LENGTH);
        String normalizedInstagramId = normalizeOptionalText("instagramId", instagramId, MAX_INSTAGRAM_ID_LENGTH);

        user.updateProfile(normalizedNickname, normalizedBio, normalizedInstagramId);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UpdateProfileImageResponse uploadProfileImage(Long userId, MultipartFile file) {
        log.info("[UserProfileImage] upload start userId={}", userId);

        User user = findUserOrThrow(userId);

        String profileImageUrl;
        try {
            profileImageUrl = profileImageStorageService.storeProfileImage(userId, file);
            log.info("[UserProfileImage] upload success userId={} url={}", userId, profileImageUrl);
        } catch (RuntimeException exception) {
            log.error("[UserProfileImage] upload failed userId={} reason={}", userId, exception.getMessage(), exception);
            throw exception;
        }

        user.changeProfileImageUrl(profileImageUrl);
        User savedUser = userRepository.save(user);
        log.info("[UserProfileImage] db updated userId={} profileImageUrl={}", userId, savedUser.getProfileImageUrl());
        return UpdateProfileImageResponse.from(savedUser);
    }

    public UserBuddyCodeResponse getBuddyCode(Long userId) {
        User user = findUserOrThrow(userId);

        return UserBuddyCodeResponse.from(user);
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserOrThrow(userId);

        UserProfileResponse response = UserProfileResponse.from(user);
        log.info("[UserProfile] response includes profileImageUrl={}", response.profileImageUrl());
        return response;
    }

    @Transactional
    public ActivePingResponse pingActive(Long userId) {
        LocalDateTime lastActiveAt = activityTrackingService.touch(userId);
        return new ActivePingResponse(userId, lastActiveAt);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null) {
            return null;
        }

        String trimmedNickname = nickname.trim();
        if (trimmedNickname.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nickname must not be blank");
        }
        if (trimmedNickname.length() > MAX_NICKNAME_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "nickname must be at most " + MAX_NICKNAME_LENGTH + " characters"
            );
        }
        return trimmedNickname;
    }

    private String normalizeOptionalText(String fieldName, String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }
        if (trimmedValue.length() > maxLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be at most " + maxLength + " characters"
            );
        }
        return trimmedValue;
    }
}
