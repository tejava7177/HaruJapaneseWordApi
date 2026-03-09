package com.haru.api.tsuntsun.controller;

import com.haru.api.tsuntsun.dto.AnswerTsunTsunRequest;
import com.haru.api.tsuntsun.dto.SendTsunTsunRequest;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.service.TsunTsunService;
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
@RequestMapping("/api/tsuntsun")
@RequiredArgsConstructor
public class TsunTsunController {

    private final TsunTsunService tsunTsunService;

    @PostMapping
    public TsunTsunQuizResponse sendTsunTsun(@Valid @RequestBody SendTsunTsunRequest request) {
        return tsunTsunService.sendTsunTsun(request.senderId(), request.receiverId(), request.dailyWordItemId());
    }

    @PostMapping("/answer")
    public TsunTsunAnswerResponse answerTsunTsun(@Valid @RequestBody AnswerTsunTsunRequest request) {
        return tsunTsunService.answerTsunTsun(request.tsuntsunId(), request.meaningId());
    }

    @GetMapping("/today")
    public List<TsunTsunQuizResponse> getTodayTsunTsuns(@RequestParam Long userId) {
        return tsunTsunService.getTodayTsunTsuns(userId);
    }
}
