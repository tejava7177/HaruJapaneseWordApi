package com.haru.api.dailyword.controller;

import com.haru.api.dailyword.dto.DevDailyWordRegenerateResponse;
import com.haru.api.dailyword.service.DailyWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/daily-words")
@RequiredArgsConstructor
public class DevDailyWordController {

    private final DailyWordService dailyWordService;

    @PostMapping("/{userId}/regenerate-today")
    public DevDailyWordRegenerateResponse regenerateTodayWords(@PathVariable Long userId) {
        return dailyWordService.regenerateTodayWordsForDevelopment(userId);
    }
}
