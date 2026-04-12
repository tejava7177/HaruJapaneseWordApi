package com.haru.api.reviewword.controller;

import com.haru.api.reviewword.dto.ReviewWordListResponse;
import com.haru.api.reviewword.dto.ReviewWordMigrationRequest;
import com.haru.api.reviewword.dto.ReviewWordMigrationResponse;
import com.haru.api.reviewword.dto.ReviewWordStatusResponse;
import com.haru.api.reviewword.service.ReviewWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/review-words")
@RequiredArgsConstructor
@Tag(name = "ReviewWords")
public class ReviewWordController {

    private final ReviewWordService reviewWordService;

    @GetMapping
    @Operation(summary = "사용자 복습 단어 목록 조회")
    public ReviewWordListResponse getReviewWords(@PathVariable Long userId) {
        return reviewWordService.getReviewWords(userId);
    }

    @PutMapping("/{wordId}")
    @Operation(summary = "복습 단어 추가")
    public ReviewWordStatusResponse addReviewWord(@PathVariable Long userId, @PathVariable Long wordId) {
        return reviewWordService.addReviewWord(userId, wordId);
    }

    @DeleteMapping("/{wordId}")
    @Operation(summary = "복습 단어 해제")
    public ReviewWordStatusResponse removeReviewWord(@PathVariable Long userId, @PathVariable Long wordId) {
        return reviewWordService.removeReviewWord(userId, wordId);
    }

    @PostMapping("/migration")
    @Operation(summary = "로컬 복습 단어 서버 마이그레이션")
    public ReviewWordMigrationResponse migrateReviewWords(
            @PathVariable Long userId,
            @Valid @RequestBody ReviewWordMigrationRequest request
    ) {
        return reviewWordService.migrateReviewWords(userId, request);
    }
}
