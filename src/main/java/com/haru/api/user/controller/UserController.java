package com.haru.api.user.controller;

import com.haru.api.user.dto.UpdateLearningLevelRequest;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{userId}/learning-level")
    public UpdateLearningLevelResponse updateLearningLevel(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateLearningLevelRequest request
    ) {
        return userService.updateLearningLevel(userId, request.learningLevel());
    }
}
