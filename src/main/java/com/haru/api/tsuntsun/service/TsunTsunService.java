package com.haru.api.tsuntsun.service;

import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.push.ApnsPushService;
import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunAnswer;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunDirection;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayItemResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayStatus;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.MeaningRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TsunTsunService {

    private static final int DAILY_SEND_LIMIT_PER_BUDDY = 10;
    private static final long GIVE_UP_CHOICE_ID = -1L;

    private final TsunTsunRepository tsunTsunRepository;
    private final TsunTsunAnswerRepository tsunTsunAnswerRepository;
    private final UserRepository userRepository;
    private final BuddyRepository buddyRepository;
    private final DailyWordItemRepository dailyWordItemRepository;
    private final DailyWordSetRepository dailyWordSetRepository;
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

        validateBuddyRelation(senderId, receiverId);

        LocalDate today = LocalDate.now();

        long pairSendCount = tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(senderId, receiverId, today);
        if (pairSendCount >= DAILY_SEND_LIMIT_PER_BUDDY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "버디 페어당 하루 최대 10회까지만 전송할 수 있습니다.");
        }

        boolean hasPending = tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(
                senderId,
                receiverId,
                today,
                TsunTsunStatus.SENT
        );
        if (hasPending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상대가 아직 이전 츤츤에 답하지 않았습니다.");
        }

        boolean alreadySentSameItem = tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(
                senderId,
                receiverId,
                dailyWordItemId,
                today
        );
        if (alreadySentSameItem) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오늘 이미 보낸 단어입니다. 같은 단어는 재전송할 수 없습니다.");
        }

        DailyWordItem dailyWordItem = dailyWordItemRepository.findById(dailyWordItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DailyWordItem not found: " + dailyWordItemId));

        if (!dailyWordItem.getDailyWordSet().getUser().getId().equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dailyWordItem은 receiver의 오늘 단어여야 합니다.");
        }

        if (!dailyWordItem.getDailyWordSet().getTargetDate().equals(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오늘의 dailyWordItem만 전송할 수 있습니다.");
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
    public TsunTsunTodayResponse getTodayTsunTsuns(Long userId, Long buddyId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
        if (!userRepository.existsById(buddyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + buddyId);
        }
        validateBuddyRelation(userId, buddyId);

        LocalDate today = LocalDate.now();

        DailyWordSet buddySet = dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(buddyId, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buddy daily words not found for today"));

        List<TsunTsun> pairTsuns = tsunTsunRepository.findPairByTargetDate(userId, buddyId, today);

        long sentCount = pairTsuns.stream()
                .filter(t -> t.getSender().getId().equals(userId) && t.getReceiver().getId().equals(buddyId))
                .count();

        long receivedCount = pairTsuns.stream()
                .filter(t -> t.getSender().getId().equals(buddyId) && t.getReceiver().getId().equals(userId))
                .count();

        Map<Long, TsunTsun> sentByItem = pairTsuns.stream()
                .filter(t -> t.getSender().getId().equals(userId) && t.getReceiver().getId().equals(buddyId))
                .collect(java.util.stream.Collectors.toMap(
                        t -> t.getDailyWordItem().getId(),
                        Function.identity(),
                        (oldValue, newValue) -> oldValue
                ));

        Map<Long, TsunTsun> receivedByItem = pairTsuns.stream()
                .filter(t -> t.getSender().getId().equals(buddyId) && t.getReceiver().getId().equals(userId))
                .collect(java.util.stream.Collectors.toMap(
                        t -> t.getDailyWordItem().getId(),
                        Function.identity(),
                        (oldValue, newValue) -> oldValue
                ));

        List<TsunTsunTodayItemResponse> items = buddySet.getItems().stream()
                .map(item -> {
                    TsunTsun sent = sentByItem.get(item.getId());
                    if (sent != null) {
                        return new TsunTsunTodayItemResponse(
                                item.getId(),
                                item.getWord().getId(),
                                TsunTsunDirection.SENT,
                                toTodayStatus(sent.getStatus())
                        );
                    }

                    TsunTsun received = receivedByItem.get(item.getId());
                    if (received != null) {
                        return new TsunTsunTodayItemResponse(
                                item.getId(),
                                item.getWord().getId(),
                                TsunTsunDirection.RECEIVED,
                                toTodayStatus(received.getStatus())
                        );
                    }

                    return new TsunTsunTodayItemResponse(
                            item.getId(),
                            item.getWord().getId(),
                            TsunTsunDirection.NONE,
                            TsunTsunTodayStatus.NONE
                    );
                })
                .toList();

        return new TsunTsunTodayResponse(userId, buddyId, today, sentCount, receivedCount, items);
    }

    private TsunTsunTodayStatus toTodayStatus(TsunTsunStatus status) {
        return switch (status) {
            case SENT -> TsunTsunTodayStatus.SENT;
            case ANSWERED -> TsunTsunTodayStatus.ANSWERED;
        };
    }

    private void validateBuddyRelation(Long userId, Long buddyId) {
        boolean connected = buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(userId, buddyId, BuddyStatus.ACTIVE)
                && buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(buddyId, userId, BuddyStatus.ACTIVE);

        if (!connected) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "버디로 연결된 사용자에게만 츤츤을 보낼 수 있습니다.");
        }
    }
}
