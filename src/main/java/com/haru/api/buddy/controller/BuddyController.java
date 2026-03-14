package com.haru.api.buddy.controller;

import com.haru.api.buddy.dto.BuddyResponse;
import com.haru.api.buddy.dto.ConnectBuddyRequest;
import com.haru.api.buddy.dto.CreateBuddyRequest;
import com.haru.api.buddy.dto.DeleteBuddyResponse;
import com.haru.api.buddy.dto.RandomMatchingCandidateResponse;
import com.haru.api.buddy.service.BuddyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buddies")
@RequiredArgsConstructor
@Tag(name = "Buddy")
public class BuddyController {

    private final BuddyService buddyService;

    @GetMapping
    @Operation(summary = "버디 목록 조회")
    public List<BuddyResponse> getBuddies(@RequestParam Long userId) {
        return buddyService.getBuddies(userId);
    }

    @PostMapping
    @Operation(summary = "버디 직접 연결")
    public BuddyResponse addBuddy(@Valid @RequestBody CreateBuddyRequest request) {
        return buddyService.addBuddy(request.userId(), request.buddyUserId());
    }

    @PostMapping("/connect")
    @Operation(summary = "초대코드로 버디 연결")
    public BuddyResponse connectBuddy(@Valid @RequestBody ConnectBuddyRequest request) {
        return buddyService.connectByBuddyCode(request.userId(), request.buddyCode());
    }

    @DeleteMapping
    @Operation(summary = "버디 삭제")
    public DeleteBuddyResponse deleteBuddy(@RequestParam Long userId, @RequestParam Long buddyUserId) {
        return buddyService.removeBuddy(userId, buddyUserId);
    }

    @GetMapping("/random-candidates")
    @Operation(summary = "랜덤 매칭 후보 조회")
    public List<RandomMatchingCandidateResponse> getRandomCandidates(@RequestParam Long userId) {
        return buddyService.getRandomCandidates(userId);
    }
}
