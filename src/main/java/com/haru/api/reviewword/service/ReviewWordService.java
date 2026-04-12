package com.haru.api.reviewword.service;

import com.haru.api.reviewword.domain.ReviewWord;
import com.haru.api.reviewword.dto.ReviewWordListResponse;
import com.haru.api.reviewword.dto.ReviewWordMigrationRequest;
import com.haru.api.reviewword.dto.ReviewWordMigrationResponse;
import com.haru.api.reviewword.dto.ReviewWordStatusResponse;
import com.haru.api.reviewword.repository.ReviewWordRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.WordRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewWordService {

    private final ReviewWordRepository reviewWordRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    public ReviewWordListResponse getReviewWords(Long userId) {
        ensureUserExists(userId);
        List<Long> wordIds = reviewWordRepository.findByUserIdOrderByWordIdAsc(userId).stream()
                .map(reviewWord -> reviewWord.getWord().getId())
                .toList();
        return new ReviewWordListResponse(userId, wordIds);
    }

    @Transactional
    public ReviewWordStatusResponse addReviewWord(Long userId, Long wordId) {
        User user = findUserOrThrow(userId);
        Word word = findWordOrThrow(wordId);

        if (reviewWordRepository.existsByUserIdAndWordId(userId, wordId)) {
            return new ReviewWordStatusResponse(userId, wordId, true);
        }

        reviewWordRepository.save(ReviewWord.create(user, word));
        return new ReviewWordStatusResponse(userId, wordId, true);
    }

    @Transactional
    public ReviewWordStatusResponse removeReviewWord(Long userId, Long wordId) {
        ensureUserExists(userId);
        ensureWordExists(wordId);

        reviewWordRepository.findByUserIdAndWordId(userId, wordId)
                .ifPresent(reviewWordRepository::delete);

        return new ReviewWordStatusResponse(userId, wordId, false);
    }

    @Transactional
    public ReviewWordMigrationResponse migrateReviewWords(Long userId, ReviewWordMigrationRequest request) {
        User user = findUserOrThrow(userId);
        List<Long> requestedWordIds = request.wordIds();
        Set<Long> deduplicatedWordIds = new LinkedHashSet<>(requestedWordIds);

        int migratedCount = 0;
        for (Long wordId : deduplicatedWordIds) {
            Word word = findWordOrThrow(wordId);
            if (reviewWordRepository.existsByUserIdAndWordId(userId, wordId)) {
                continue;
            }
            reviewWordRepository.save(ReviewWord.create(user, word));
            migratedCount += 1;
        }

        return new ReviewWordMigrationResponse(userId, migratedCount, requestedWordIds.size());
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private Word findWordOrThrow(Long wordId) {
        return wordRepository.findById(wordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found: " + wordId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }

    private void ensureWordExists(Long wordId) {
        if (!wordRepository.existsById(wordId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found: " + wordId);
        }
    }
}
