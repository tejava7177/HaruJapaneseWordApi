package com.haru.api.user.service;

import com.haru.api.user.domain.User;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdateProfileImageResponse;
import com.haru.api.user.dto.UpdateRandomMatchingResponse;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.dto.UserProfileResponse;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
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

    private final UserRepository userRepository;
    private final ProfileImageStorageService profileImageStorageService;

    @Transactional
    public UpdateLearningLevelResponse updateLearningLevel(Long userId, WordLevel learningLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        user.changeLearningLevel(learningLevel);
        return UpdateLearningLevelResponse.from(user);
    }

    @Transactional
    public UpdateRandomMatchingResponse updateRandomMatchingEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        user.changeRandomMatchingEnabled(enabled);
        return UpdateRandomMatchingResponse.from(user);
    }

    @Transactional
    public UpdateProfileImageResponse uploadProfileImage(Long userId, MultipartFile file) {
        log.info("[UserProfileImage] upload start userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        String profileImageUrl;
        try {
            profileImageUrl = profileImageStorageService.storeProfileImage(userId, file);
            log.info("[UserProfileImage] upload success userId={} url={}", userId, profileImageUrl);
        } catch (RuntimeException exception) {
            log.error("[UserProfileImage] upload failed userId={} reason={}", userId, exception.getMessage(), exception);
            throw exception;
        }

        user.changeProfileImageUrl(profileImageUrl);
        log.info("[UserProfileImage] db updated userId={}", userId);
        return UpdateProfileImageResponse.from(user);
    }

    public UserBuddyCodeResponse getBuddyCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        return UserBuddyCodeResponse.from(user);
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        return UserProfileResponse.from(user);
    }
}
