package com.haru.api.dailyword.controller;

import com.haru.api.dailyword.dto.DailyWordTodayResponse;
import com.haru.api.dailyword.service.DailyWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-words")
@RequiredArgsConstructor
public class DailyWordController {

    private final DailyWordService dailyWordService;

    @GetMapping("/today")
    public DailyWordTodayResponse getTodayWords(@RequestParam Long userId) {
        return dailyWordService.getTodayWords(userId);
    }
}
