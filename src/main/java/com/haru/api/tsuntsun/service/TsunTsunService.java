package com.haru.api.tsuntsun.service;

import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.push.ApnsPushService;
import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunAnswer;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.MeaningRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TsunTsunService {

    private static final int DAILY_SEND_LIMIT = 10;
    private static final long GIVE_UP_CHOICE_ID = -1L;

    private final TsunTsunRepository tsunTsunRepository;
    private final TsunTsunAnswerRepository tsunTsunAnswerRepository;
    private final UserRepository userRepository;
    private final DailyWordItemRepository dailyWordItemRepository;
    private final MeaningRepository meaningRepository;
    private final TsunTsunQuizService tsunTsunQuizService;
    private final ApnsPushService apnsPushService;

    @Transactional
    public TsunTsunQuizResponse sendTsunTsun(Long senderId, Long receiverId, Long dailyWordItemId) {
        if (senderId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender and receiver must be different");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + receiverId));

        LocalDate today = LocalDate.now();

        long todaySendCount = tsunTsunRepository.countBySenderIdAndTargetDate(senderId, today);
        if (todaySendCount >= DAILY_SEND_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily tsuntsun send limit(10) exceeded");
        }

        boolean alreadySent = tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDateAndStatus(
                senderId,
                receiverId,
                dailyWordItemId,
                today,
                TsunTsunStatus.SENT
        );
        if (alreadySent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already sent tsuntsun with this item today");
        }

        DailyWordItem dailyWordItem = dailyWordItemRepository.findById(dailyWordItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DailyWordItem not found: " + dailyWordItemId));

        if (!dailyWordItem.getDailyWordSet().getUser().getId().equals(senderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dailyWordItem does not belong to sender");
        }

        Word word = dailyWordItem.getWord();

        TsunTsun saved = tsunTsunRepository.save(TsunTsun.sent(sender, receiver, word, dailyWordItem, today));

        List<QuizChoiceResponse> choices = tsunTsunQuizService.generateChoices(word);
        apnsPushService.sendTsunTsunPush(receiverId, saved.getId());

        return TsunTsunQuizResponse.from(saved, choices);
    }

    @Transactional
    public TsunTsunAnswerResponse answerTsunTsun(Long tsuntsunId, Long meaningId) {
        TsunTsun tsunTsun = tsunTsunRepository.findWithWordById(tsuntsunId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TsunTsun not found: " + tsuntsunId));

        if (tsunTsun.getStatus() == TsunTsunStatus.ANSWERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TsunTsun already answered");
        }

        List<Meaning> meanings = meaningRepository.findByWordIdOrderByOrdAsc(tsunTsun.getWord().getId());
        if (meanings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Word has no meaning: " + tsunTsun.getWord().getId());
        }

        Meaning correctMeaning = meanings.get(0);
        boolean isCorrect;
        String selectedMeaningText;

        if (meaningId == GIVE_UP_CHOICE_ID) {
            isCorrect = false;
            selectedMeaningText = "모르겠어요";
        } else {
            Meaning selectedMeaning = meanings.stream()
                    .filter(meaning -> meaning.getId().equals(meaningId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meaning not valid for this quiz"));

            isCorrect = selectedMeaning.getId().equals(correctMeaning.getId());
            selectedMeaningText = selectedMeaning.getText();
        }

        tsunTsun.markAnswered();
        tsunTsunAnswerRepository.save(TsunTsunAnswer.of(tsunTsun, selectedMeaningText, isCorrect));

        return new TsunTsunAnswerResponse(
                tsuntsunId,
                isCorrect,
                selectedMeaningText,
                correctMeaning.getId(),
                correctMeaning.getText(),
                tsunTsun.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<TsunTsunQuizResponse> getTodayTsunTsuns(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }

        LocalDate today = LocalDate.now();
        return tsunTsunRepository.findByReceiverIdAndTargetDateOrderByCreatedAtDesc(userId, today).stream()
                .map(tsunTsun -> {
                    List<QuizChoiceResponse> choices = tsunTsun.getStatus() == TsunTsunStatus.SENT
                            ? tsunTsunQuizService.generateChoices(tsunTsun.getWord())
                            : Collections.emptyList();
                    return TsunTsunQuizResponse.from(tsunTsun, choices);
                })
                .toList();
    }
}
