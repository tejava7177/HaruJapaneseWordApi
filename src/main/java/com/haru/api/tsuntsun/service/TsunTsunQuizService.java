package com.haru.api.tsuntsun.service;

import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.MeaningRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private final MeaningRepository meaningRepository;

    public List<QuizChoiceResponse> generateChoices(Word word) {
        List<Meaning> wordMeanings = meaningRepository.findByWordIdOrderByOrdAsc(word.getId());
        if (wordMeanings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Word has no meaning: " + word.getId());
        }

        Meaning correctMeaning = wordMeanings.get(0);

        List<Meaning> wrongPool = meaningRepository.findByWordIdNot(word.getId());
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
}
