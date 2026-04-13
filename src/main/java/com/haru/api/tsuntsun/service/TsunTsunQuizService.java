package com.haru.api.tsuntsun.service;

import com.haru.api.tsuntsun.domain.TsunTsunQuizType;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.MeaningRepository;
import com.haru.api.word.repository.WordRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TsunTsunQuizService {

    private static final long GIVE_UP_CHOICE_ID = -1L;
    private static final int READING_WRONG_CHOICE_COUNT = 3;

    private final MeaningRepository meaningRepository;
    private final WordRepository wordRepository;

    public TsunTsunQuizType pickQuizType() {
        return ThreadLocalRandom.current().nextBoolean() ? TsunTsunQuizType.MEANING : TsunTsunQuizType.READING;
    }

    public TsunTsunGeneratedQuiz generateQuiz(Word word, TsunTsunQuizType type) {
        return new TsunTsunGeneratedQuiz(type, switch (type) {
            case MEANING -> generateMeaningChoices(word);
            case READING -> generateReadingChoices(word);
        });
    }

    private List<QuizChoiceResponse> generateMeaningChoices(Word word) {
        List<Meaning> wordMeanings = meaningRepository.findByWordIdOrderByOrdAsc(word.getId());
        if (wordMeanings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Word has no meaning: " + word.getId());
        }

        Meaning correctMeaning = wordMeanings.get(0);
        List<Meaning> wrongPool = new ArrayList<>(meaningRepository.findByWordIdNot(word.getId()));
        Collections.shuffle(wrongPool);

        List<QuizChoiceResponse> choices = new ArrayList<>();
        choices.add(new QuizChoiceResponse(correctMeaning.getId(), correctMeaning.getText()));

        int wrongChoiceCount = Math.min(2, wrongPool.size());
        for (int i = 0; i < wrongChoiceCount; i++) {
            Meaning wrongMeaning = wrongPool.get(i);
            choices.add(new QuizChoiceResponse(wrongMeaning.getId(), wrongMeaning.getText()));
        }

        choices.add(new QuizChoiceResponse(GIVE_UP_CHOICE_ID, "모르겠어요"));
        Collections.shuffle(choices);
        return choices;
    }

    private List<QuizChoiceResponse> generateReadingChoices(Word word) {
        String correctReading = word.getReading();
        if (isBlank(correctReading)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Word has no reading: " + word.getId());
        }

        Set<String> usedReadings = new HashSet<>();
        List<Word> wrongWords = wordRepository.findByLevelAndIdNotOrderByIdAsc(word.getLevel(), word.getId()).stream()
                .filter(candidate -> !isBlank(candidate.getReading()))
                .filter(candidate -> !correctReading.equals(candidate.getReading()))
                .filter(candidate -> usedReadings.add(candidate.getReading()))
                .sorted(Comparator.comparingDouble(candidate -> readingSimilarityScore(correctReading, candidate.getReading())))
                .limit(READING_WRONG_CHOICE_COUNT)
                .toList();

        if (wrongWords.size() < READING_WRONG_CHOICE_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough reading quiz candidates for word: " + word.getId());
        }

        List<QuizChoiceResponse> choices = new ArrayList<>();
        choices.add(new QuizChoiceResponse(word.getId(), correctReading));
        wrongWords.forEach(wrongWord -> choices.add(new QuizChoiceResponse(wrongWord.getId(), wrongWord.getReading())));
        Collections.shuffle(choices);
        return choices;
    }

    private double readingSimilarityScore(String answerReading, String candidateReading) {
        return (Math.abs(answerReading.length() - candidateReading.length()) * 10.0)
                + weightedKanaDistance(answerReading, candidateReading)
                - commonPrefixLength(answerReading, candidateReading);
    }

    private double weightedKanaDistance(String source, String target) {
        int sourceLength = source.length();
        int targetLength = target.length();
        double[][] distance = new double[sourceLength + 1][targetLength + 1];

        for (int i = 1; i <= sourceLength; i++) {
            distance[i][0] = distance[i - 1][0] + deletionCost(source, i - 1);
        }
        for (int j = 1; j <= targetLength; j++) {
            distance[0][j] = distance[0][j - 1] + deletionCost(target, j - 1);
        }

        for (int i = 1; i <= sourceLength; i++) {
            for (int j = 1; j <= targetLength; j++) {
                double substitution = distance[i - 1][j - 1]
                        + substitutionCost(source.charAt(i - 1), target.charAt(j - 1));
                double deletion = distance[i - 1][j] + deletionCost(source, i - 1);
                double insertion = distance[i][j - 1] + deletionCost(target, j - 1);
                distance[i][j] = Math.min(substitution, Math.min(deletion, insertion));
            }
        }

        return distance[sourceLength][targetLength];
    }

    private double substitutionCost(char left, char right) {
        if (left == right) {
            return 0.0;
        }
        if (normalizeSmallKana(left) == normalizeSmallKana(right)) {
            return 0.2;
        }
        if (baseKana(left) == baseKana(right)) {
            return 0.35;
        }
        if (vowelOf(left) != 0 && vowelOf(left) == vowelOf(right)) {
            return 0.45;
        }
        return 1.0;
    }

    private double deletionCost(String text, int index) {
        char current = text.charAt(index);
        if (current == 'っ' || current == 'ー') {
            return 0.25;
        }
        if (index > 0 && vowelOf(text.charAt(index - 1)) != 0 && vowelOf(text.charAt(index - 1)) == vowelOf(current)) {
            return 0.35;
        }
        return 1.0;
    }

    private int commonPrefixLength(String left, String right) {
        int limit = Math.min(left.length(), right.length());
        int length = 0;
        while (length < limit && left.charAt(length) == right.charAt(length)) {
            length++;
        }
        return length;
    }

    private char normalizeSmallKana(char value) {
        return switch (value) {
            case 'ぁ' -> 'あ';
            case 'ぃ' -> 'い';
            case 'ぅ' -> 'う';
            case 'ぇ' -> 'え';
            case 'ぉ' -> 'お';
            case 'ゃ' -> 'や';
            case 'ゅ' -> 'ゆ';
            case 'ょ' -> 'よ';
            case 'ゎ' -> 'わ';
            case 'っ' -> 'つ';
            default -> value;
        };
    }

    private char baseKana(char value) {
        return switch (normalizeSmallKana(value)) {
            case 'が', 'か' -> 'か';
            case 'ぎ', 'き' -> 'き';
            case 'ぐ', 'く' -> 'く';
            case 'げ', 'け' -> 'け';
            case 'ご', 'こ' -> 'こ';
            case 'ざ', 'さ' -> 'さ';
            case 'じ', 'し' -> 'し';
            case 'ず', 'す' -> 'す';
            case 'ぜ', 'せ' -> 'せ';
            case 'ぞ', 'そ' -> 'そ';
            case 'だ', 'た' -> 'た';
            case 'ぢ', 'ち' -> 'ち';
            case 'づ', 'つ' -> 'つ';
            case 'で', 'て' -> 'て';
            case 'ど', 'と' -> 'と';
            case 'ば', 'ぱ', 'は' -> 'は';
            case 'び', 'ぴ', 'ひ' -> 'ひ';
            case 'ぶ', 'ぷ', 'ふ' -> 'ふ';
            case 'べ', 'ぺ', 'へ' -> 'へ';
            case 'ぼ', 'ぽ', 'ほ' -> 'ほ';
            default -> normalizeSmallKana(value);
        };
    }

    private char vowelOf(char value) {
        return switch (normalizeSmallKana(value)) {
            case 'あ', 'か', 'が', 'さ', 'ざ', 'た', 'だ', 'な', 'は', 'ば', 'ぱ', 'ま', 'や', 'ら', 'わ' -> 'a';
            case 'い', 'き', 'ぎ', 'し', 'じ', 'ち', 'ぢ', 'に', 'ひ', 'び', 'ぴ', 'み', 'り' -> 'i';
            case 'う', 'く', 'ぐ', 'す', 'ず', 'つ', 'づ', 'ぬ', 'ふ', 'ぶ', 'ぷ', 'む', 'ゆ', 'る' -> 'u';
            case 'え', 'け', 'げ', 'せ', 'ぜ', 'て', 'で', 'ね', 'へ', 'べ', 'ぺ', 'め', 'れ' -> 'e';
            case 'お', 'こ', 'ご', 'そ', 'ぞ', 'と', 'ど', 'の', 'ほ', 'ぼ', 'ぽ', 'も', 'よ', 'ろ', 'を' -> 'o';
            default -> 0;
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
