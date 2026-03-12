package com.haru.api.dailyword.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.dailyword.dto.DevDailyWordRegenerateResponse;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.WordRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DailyWordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private DailyWordSetRepository dailyWordSetRepository;

    @Mock
    private TsunTsunRepository tsunTsunRepository;

    @Mock
    private TsunTsunAnswerRepository tsunTsunAnswerRepository;

    private DailyWordService dailyWordService;

    @BeforeEach
    void setUp() {
        dailyWordService = new DailyWordService(
                userRepository,
                wordRepository,
                dailyWordSetRepository,
                tsunTsunRepository,
                tsunTsunAnswerRepository
        );
    }

    @Test
    void regenerateTodayWordsForDevelopment_recreatesWhenExistingSetPresent() {
        User user = new User(4L, "buddy4", WordLevel.N2, "BUDDY004");
        DailyWordSet existingSet = DailyWordSet.create(user, LocalDate.now(), WordLevel.N4);
        existingSet.addItem(new Word("old1", "old1", WordLevel.N4), 1);
        existingSet.addItem(new Word("old2", "old2", WordLevel.N4), 2);
        ReflectionTestUtils.setField(existingSet.getItems().get(0), "id", 101L);
        ReflectionTestUtils.setField(existingSet.getItems().get(1), "id", 102L);
        TsunTsun tsunTsun = TsunTsun.sent(user, new User(1L, "juheun", WordLevel.N4, "JUHEUN01"),
                existingSet.getItems().get(0).getWord(), existingSet.getItems().get(0), LocalDate.now());
        ReflectionTestUtils.setField(tsunTsun, "id", 1001L);

        given(userRepository.findById(4L)).willReturn(Optional.of(user));
        given(dailyWordSetRepository.findByUserIdAndTargetDate(4L, LocalDate.now())).willReturn(Optional.of(existingSet));
        given(tsunTsunRepository.findByDailyWordItemIdInAndTargetDate(List.of(101L, 102L), LocalDate.now()))
                .willReturn(List.of(tsunTsun));
        given(wordRepository.findByLevelOrderByIdAsc(WordLevel.N2)).willReturn(wordsForLevel(WordLevel.N2));
        given(dailyWordSetRepository.saveAndFlush(any(DailyWordSet.class))).willAnswer(invocation -> invocation.getArgument(0));

        DevDailyWordRegenerateResponse response = dailyWordService.regenerateTodayWordsForDevelopment(4L);

        verify(tsunTsunAnswerRepository).deleteByTsuntsunIdIn(List.of(1001L));
        verify(tsunTsunAnswerRepository).flush();
        verify(tsunTsunRepository).deleteByDailyWordItemIdInAndTargetDate(List.of(101L, 102L), LocalDate.now());
        verify(tsunTsunRepository).flush();
        verify(dailyWordSetRepository).delete(existingSet);
        verify(dailyWordSetRepository).flush();
        assertThat(response.userId()).isEqualTo(4L);
        assertThat(response.level()).isEqualTo(WordLevel.N2);
        assertThat(response.itemCount()).isEqualTo(10);
    }

    @Test
    void regenerateTodayWordsForDevelopment_createsWhenNoExistingSet() {
        User user = new User(4L, "buddy4", WordLevel.N2, "BUDDY004");

        given(userRepository.findById(4L)).willReturn(Optional.of(user));
        given(dailyWordSetRepository.findByUserIdAndTargetDate(4L, LocalDate.now())).willReturn(Optional.empty());
        given(wordRepository.findByLevelOrderByIdAsc(WordLevel.N2)).willReturn(wordsForLevel(WordLevel.N2));
        given(dailyWordSetRepository.saveAndFlush(any(DailyWordSet.class))).willAnswer(invocation -> invocation.getArgument(0));

        DevDailyWordRegenerateResponse response = dailyWordService.regenerateTodayWordsForDevelopment(4L);

        assertThat(response.userId()).isEqualTo(4L);
        assertThat(response.targetDate()).isEqualTo(LocalDate.now());
        assertThat(response.itemCount()).isEqualTo(10);
    }

    @Test
    void regenerateTodayWordsForDevelopment_failsWhenUserMissing() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> dailyWordService.regenerateTodayWordsForDevelopment(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 99");
    }

    private List<Word> wordsForLevel(WordLevel level) {
        return java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(index -> {
                    Word word = new Word("word" + index, "reading" + index, level);
                    ReflectionTestUtils.setField(word, "id", (long) index);
                    return word;
                })
                .toList();
    }
}
