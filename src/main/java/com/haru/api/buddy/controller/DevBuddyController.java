package com.haru.api.buddy.controller;

import com.haru.api.buddy.dto.DevBuddyResetResponse;
import com.haru.api.buddy.service.BuddyDevelopmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/buddies")
@RequiredArgsConstructor
public class DevBuddyController {

    private final BuddyDevelopmentService buddyDevelopmentService;

    @PostMapping("/reset")
    public DevBuddyResetResponse resetBuddyData() {
        return buddyDevelopmentService.resetBuddyData();
    }
}
