package com.haru.api.word.controller;

import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.dto.WordDetailResponse;
import com.haru.api.word.dto.WordSimpleResponse;
import com.haru.api.word.service.WordService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/{wordId}")
    public WordDetailResponse getWord(@PathVariable Long wordId) {
        return wordService.getWord(wordId);
    }

    @GetMapping
    public List<WordSimpleResponse> getWords(@RequestParam(required = false) WordLevel level) {
        return wordService.getWords(level);
    }
}
