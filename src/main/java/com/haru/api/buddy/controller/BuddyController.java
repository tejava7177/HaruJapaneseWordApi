package com.haru.api.buddy.controller;

import com.haru.api.buddy.dto.BuddyResponse;
import com.haru.api.buddy.dto.CreateBuddyRequest;
import com.haru.api.buddy.service.BuddyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buddies")
@RequiredArgsConstructor
public class BuddyController {

    private final BuddyService buddyService;

    @GetMapping
    public List<BuddyResponse> getBuddies(@RequestParam Long userId) {
        return buddyService.getBuddies(userId);
    }

    @PostMapping
    public BuddyResponse addBuddy(@Valid @RequestBody CreateBuddyRequest request) {
        return buddyService.addBuddy(request.userId(), request.buddyUserId());
    }
}
