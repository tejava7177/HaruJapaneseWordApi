package com.haru.api.user.service;

import com.haru.api.user.domain.User;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdateRandomMatchingResponse;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

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

    public UserBuddyCodeResponse getBuddyCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        return UserBuddyCodeResponse.from(user);
    }
}
