package com.haru.api.word.service;

import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.dto.WordDetailResponse;
import com.haru.api.word.dto.WordSimpleResponse;
import com.haru.api.word.repository.WordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordService {

    private final WordRepository wordRepository;

    public WordDetailResponse getWord(Long wordId) {
        Word word = wordRepository.findWithMeaningsById(wordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found: " + wordId));
        return WordDetailResponse.from(word);
    }

    public List<WordSimpleResponse> getWords(WordLevel level) {
        List<Word> words = (level == null)
                ? wordRepository.findTop20ByOrderByIdAsc()
                : wordRepository.findByLevelOrderByIdAsc(level);

        return words.stream()
                .map(WordSimpleResponse::from)
                .toList();
    }
}
