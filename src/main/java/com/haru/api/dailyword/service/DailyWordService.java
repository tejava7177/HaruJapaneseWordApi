package com.haru.api.dailyword.service;

import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.dailyword.dto.DevDailyWordRegenerateResponse;
import com.haru.api.dailyword.dto.DailyWordTodayResponse;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.WordRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DailyWordService {

    private static final int DAILY_WORD_COUNT = 10;

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final DailyWordSetRepository dailyWordSetRepository;
    private final TsunTsunRepository tsunTsunRepository;
    private final TsunTsunAnswerRepository tsunTsunAnswerRepository;
    private final Clock clock;

    @Transactional
    public DailyWordTodayResponse getTodayWords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        LocalDate today = LocalDate.now(clock);

        return dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(userId, today)
                .map(DailyWordTodayResponse::from)
                .orElseGet(() -> createTodayWords(user, today));
    }

    @Transactional
    public DevDailyWordRegenerateResponse regenerateTodayWordsForDevelopment(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        LocalDate today = LocalDate.now(clock);
        dailyWordSetRepository.findByUserIdAndTargetDate(userId, today)
                .ifPresent(existingSet -> {
                    List<Long> dailyWordItemIds = existingSet.getItems().stream()
                            .map(item -> item.getId())
                            .toList();

                    if (!dailyWordItemIds.isEmpty()) {
                        List<Long> tsuntsunIds = tsunTsunRepository.findByDailyWordItemIdInAndTargetDate(dailyWordItemIds, today)
                                .stream()
                                .map(TsunTsun::getId)
                                .toList();

                        if (!tsuntsunIds.isEmpty()) {
                            tsunTsunAnswerRepository.deleteByTsuntsunIdIn(tsuntsunIds);
                            tsunTsunAnswerRepository.flush();
                        }

                        tsunTsunRepository.deleteByDailyWordItemIdInAndTargetDate(dailyWordItemIds, today);
                        tsunTsunRepository.flush();
                    }

                    dailyWordSetRepository.delete(existingSet);
                    dailyWordSetRepository.flush();
                });

        DailyWordTodayResponse regenerated = createTodayWords(user, today);
        return DevDailyWordRegenerateResponse.from(regenerated);
    }

    private DailyWordTodayResponse createTodayWords(User user, LocalDate targetDate) {
        WordLevel level = user.getLearningLevel();
        List<Word> candidates = wordRepository.findByLevelOrderByIdAsc(level);

        if (candidates.size() < DAILY_WORD_COUNT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Not enough words for level " + level + ": required 10, but got " + candidates.size()
            );
        }

        List<Word> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);
        List<Word> selected = shuffled.subList(0, DAILY_WORD_COUNT);

        DailyWordSet dailyWordSet = DailyWordSet.create(user, targetDate, level);
        for (int i = 0; i < selected.size(); i++) {
            dailyWordSet.addItem(selected.get(i), i + 1);
        }

        try {
            DailyWordSet saved = dailyWordSetRepository.saveAndFlush(dailyWordSet);
            return DailyWordTodayResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            return dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(user.getId(), targetDate)
                    .map(DailyWordTodayResponse::from)
                    .orElseThrow(() -> ex);
        }
    }
}
