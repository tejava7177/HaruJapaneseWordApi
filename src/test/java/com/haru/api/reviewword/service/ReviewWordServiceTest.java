package com.haru.api.reviewword.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.haru.api.reviewword.domain.ReviewWord;
import com.haru.api.reviewword.dto.ReviewWordListResponse;
import com.haru.api.reviewword.dto.ReviewWordMigrationRequest;
import com.haru.api.reviewword.dto.ReviewWordMigrationResponse;
import com.haru.api.reviewword.dto.ReviewWordStatusResponse;
import com.haru.api.reviewword.repository.ReviewWordRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.WordRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReviewWordServiceTest {

    @Mock
    private ReviewWordRepository reviewWordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WordRepository wordRepository;

    private ReviewWordService reviewWordService;

    @BeforeEach
    void setUp() {
        reviewWordService = new ReviewWordService(reviewWordRepository, userRepository, wordRepository);
    }

    @Test
    void getReviewWords_returnsWordIdsForUserOnly() {
        User user1 = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Word word10 = new Word("食べる", "たべる", WordLevel.N5);
        Word word30 = new Word("行く", "いく", WordLevel.N5);
        setWordId(word10, 10L);
        setWordId(word30, 30L);

        given(userRepository.existsById(1L)).willReturn(true);
        given(reviewWordRepository.findByUserIdOrderByWordIdAsc(1L))
                .willReturn(List.of(
                        ReviewWord.create(user1, word10),
                        ReviewWord.create(user1, word30)
                ));

        ReviewWordListResponse response = reviewWordService.getReviewWords(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.wordIds()).containsExactly(10L, 30L);
    }

    @Test
    void addReviewWord_returnsSuccessWithoutDuplicateInsertWhenAlreadyExists() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Word word = new Word("食べる", "たべる", WordLevel.N5);
        setWordId(word, 10L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(wordRepository.findById(10L)).willReturn(Optional.of(word));
        given(reviewWordRepository.existsByUserIdAndWordId(1L, 10L)).willReturn(true);

        ReviewWordStatusResponse response = reviewWordService.addReviewWord(1L, 10L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.wordId()).isEqualTo(10L);
        assertThat(response.reviewed()).isTrue();
        verify(reviewWordRepository, never()).save(any());
    }

    @Test
    void removeReviewWord_returnsFalseWhenItemMissing() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(wordRepository.existsById(10L)).willReturn(true);
        given(reviewWordRepository.findByUserIdAndWordId(1L, 10L)).willReturn(Optional.empty());

        ReviewWordStatusResponse response = reviewWordService.removeReviewWord(1L, 10L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.wordId()).isEqualTo(10L);
        assertThat(response.reviewed()).isFalse();
        verify(reviewWordRepository, never()).delete(any());
    }

    @Test
    void migrateReviewWords_ignoresDuplicateWordIdsInRequestAndAlreadySavedItems() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Word word10 = new Word("食べる", "たべる", WordLevel.N5);
        Word word20 = new Word("飲む", "のむ", WordLevel.N5);
        setWordId(word10, 10L);
        setWordId(word20, 20L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(wordRepository.findById(10L)).willReturn(Optional.of(word10));
        given(wordRepository.findById(20L)).willReturn(Optional.of(word20));
        given(reviewWordRepository.existsByUserIdAndWordId(1L, 10L)).willReturn(true);
        given(reviewWordRepository.existsByUserIdAndWordId(1L, 20L)).willReturn(false);

        ReviewWordMigrationResponse response = reviewWordService.migrateReviewWords(
                1L,
                new ReviewWordMigrationRequest(List.of(10L, 20L, 20L, 10L))
        );

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.migratedCount()).isEqualTo(1);
        assertThat(response.totalWordIds()).isEqualTo(4);
        verify(reviewWordRepository).save(any(ReviewWord.class));
    }

    @Test
    void addReviewWord_failsWhenWordMissing() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(wordRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewWordService.addReviewWord(1L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Word not found: 999");
    }

    private void setWordId(Word word, Long id) {
        try {
            java.lang.reflect.Field field = Word.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(word, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
