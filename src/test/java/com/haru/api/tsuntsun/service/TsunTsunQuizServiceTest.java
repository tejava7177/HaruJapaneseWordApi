package com.haru.api.tsuntsun.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.haru.api.tsuntsun.domain.TsunTsunQuizType;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.MeaningRepository;
import com.haru.api.word.repository.WordRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TsunTsunQuizServiceTest {

    @Mock private MeaningRepository meaningRepository;
    @Mock private WordRepository wordRepository;

    @Test
    void generateQuiz_buildsReadingChoicesFromSimilarSameLevelWords() {
        TsunTsunQuizService quizService = new TsunTsunQuizService(meaningRepository, wordRepository);

        Word answer = word(1L, "学校", "がっこう", WordLevel.N5);
        Word similar1 = word(2L, "学光", "がこう", WordLevel.N5);
        Word similar2 = word(3L, "学行", "がっこ", WordLevel.N5);
        Word similar3 = word(4L, "楽校", "かっこう", WordLevel.N5);
        Word far = word(5L, "明日", "あした", WordLevel.N5);

        given(wordRepository.findByLevelAndIdNotOrderByIdAsc(WordLevel.N5, 1L))
                .willReturn(List.of(far, similar1, similar2, similar3));

        TsunTsunGeneratedQuiz quiz = quizService.generateQuiz(answer, TsunTsunQuizType.READING);

        assertThat(quiz.type()).isEqualTo(TsunTsunQuizType.READING);
        assertThat(quiz.choices()).hasSize(4);
        assertThat(quiz.choices()).extracting("text")
                .contains("がっこう", "がこう", "がっこ", "かっこう")
                .doesNotContain("あした");
        assertThat(quiz.choices()).extracting("choiceId")
                .contains(1L, 2L, 3L, 4L);
    }

    @Test
    void generateQuiz_keepsExistingMeaningChoices() {
        TsunTsunQuizService quizService = new TsunTsunQuizService(meaningRepository, wordRepository);
        Word answer = word(1L, "花", "はな", WordLevel.N5);

        Meaning correct = new Meaning(answer, "꽃", 1);
        Meaning wrong1 = new Meaning(word(2L, "鼻", "はな", WordLevel.N5), "코", 1);
        Meaning wrong2 = new Meaning(word(3L, "春", "はる", WordLevel.N5), "봄", 1);
        ReflectionTestUtils.setField(correct, "id", 11L);
        ReflectionTestUtils.setField(wrong1, "id", 12L);
        ReflectionTestUtils.setField(wrong2, "id", 13L);

        given(meaningRepository.findByWordIdOrderByOrdAsc(1L)).willReturn(List.of(correct));
        given(meaningRepository.findByWordIdNot(1L)).willReturn(List.of(wrong1, wrong2));

        TsunTsunGeneratedQuiz quiz = quizService.generateQuiz(answer, TsunTsunQuizType.MEANING);

        assertThat(quiz.type()).isEqualTo(TsunTsunQuizType.MEANING);
        assertThat(quiz.choices()).extracting("text")
                .contains("꽃", "코", "봄", "모르겠어요");
    }

    private Word word(Long id, String expression, String reading, WordLevel level) {
        Word word = new Word(expression, reading, level);
        ReflectionTestUtils.setField(word, "id", id);
        return word;
    }
}
