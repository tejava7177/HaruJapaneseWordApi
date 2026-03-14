package com.haru.api.buddy.controller;

import com.haru.api.buddy.dto.DevBuddyResetResponse;
import com.haru.api.buddy.service.BuddyDevelopmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/buddies")
@RequiredArgsConstructor
@Tag(name = "Dev Buddies")
public class DevBuddyController {

    private final BuddyDevelopmentService buddyDevelopmentService;

    @PostMapping("/reset")
    @Operation(summary = "개발용 전체 buddy/reset seed 실행")
    public DevBuddyResetResponse resetBuddyData() {
        return buddyDevelopmentService.resetBuddyData();
    }
}
