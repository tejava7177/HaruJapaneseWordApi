package com.haru.api.tsuntsun.service;

import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.push.PushNotificationService;
import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunAnswer;
import com.haru.api.tsuntsun.domain.TsunTsunQuizType;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunDirection;
import com.haru.api.tsuntsun.dto.TsunTsunInboxItemResponse;
import com.haru.api.tsuntsun.dto.TsunTsunInboxResponse;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayItemResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayStatus;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.user.service.ActivityTrackingService;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.MeaningRepository;
import com.haru.api.word.repository.WordRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsunTsunService {

    private static final int DAILY_SEND_LIMIT_PER_BUDDY = 10;
    private static final int PAIR_PROGRESS_GOAL = DAILY_SEND_LIMIT_PER_BUDDY;
    private static final long GIVE_UP_CHOICE_ID = -1L;
    private static final String GIVE_UP_TEXT = "모르겠어요";
    private static final String UNKNOWN_MEANING_TEXT = "알 수 없는 뜻";
    private static final String PENDING_RECEIVED_TSUNTSUN_MESSAGE = "먼저 받은 츤츤에 답해주세요. 답변 후 새 츤츤을 보낼 수 있어요.";

    private final TsunTsunRepository tsunTsunRepository;
    private final TsunTsunAnswerRepository tsunTsunAnswerRepository;
    private final UserRepository userRepository;
    private final BuddyRepository buddyRepository;
    private final DailyWordItemRepository dailyWordItemRepository;
    private final DailyWordSetRepository dailyWordSetRepository;
    private final MeaningRepository meaningRepository;
    private final WordRepository wordRepository;
    private final TsunTsunQuizService tsunTsunQuizService;
    private final PushNotificationService pushNotificationService;
    private final ActivityTrackingService activityTrackingService;
    private final Clock clock;

    @Transactional
    public TsunTsunQuizResponse sendTsunTsun(Long senderId, Long receiverId, Long dailyWordItemId) {
        activityTrackingService.touch(senderId);
        log.info("[tsuntsun/send] start senderId={} receiverId={} dailyWordItemId={}",
                senderId, receiverId, dailyWordItemId);

        if (senderId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender and receiver must be different");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + receiverId));

        Buddy buddy = getActiveBuddy(senderId, receiverId);
        validateBuddyRelation(buddy, receiverId, senderId);

        LocalDate today = LocalDate.now(clock);

        long pairSendCount = tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(senderId, receiverId, today);
        if (pairSendCount >= DAILY_SEND_LIMIT_PER_BUDDY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "버디 페어당 하루 최대 10회까지만 전송할 수 있습니다.");
        }

        boolean hasPendingSentBySender = tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(
                senderId,
                receiverId,
                today,
                TsunTsunStatus.SENT
        );
        boolean hasPendingReceivedFromBuddy = tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(
                receiverId,
                senderId,
                today,
                TsunTsunStatus.SENT
        );

        log.info("[tsuntsun/send] pair rule check: senderId={}, receiverId={}, targetDate={}, hasPendingSentBySender={}, hasPendingReceivedFromBuddy={}",
                senderId, receiverId, today, hasPendingSentBySender, hasPendingReceivedFromBuddy);

        if (hasPendingSentBySender) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상대가 아직 이전 츤츤에 답하지 않았습니다.");
        }
        if (hasPendingReceivedFromBuddy) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PENDING_RECEIVED_TSUNTSUN_MESSAGE);
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
        TsunTsunQuizType quizType = tsunTsunQuizService.pickQuizType();
        BuddyRelationship buddyRelationship = buddy.getBuddyRelationship();
        TsunTsun saved = tsunTsunRepository.saveAndFlush(
                TsunTsun.sent(sender, receiver, word, dailyWordItem, buddyRelationship, today, quizType)
        );

        log.info("[tsuntsun/send] persisted tsuntsunId={} senderId={} receiverId={} targetDate={} status={}",
                saved.getId(), senderId, receiverId, saved.getTargetDate(), saved.getStatus());

        TsunTsunGeneratedQuiz quiz = tsunTsunQuizService.generateQuiz(word, quizType);
        Runnable pushTask = () -> pushNotificationService.notifyTsunTsunReceived(
                receiverId,
                saved.getId(),
                senderId,
                sender.getNickname()
        );
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("[tsuntsun/send] afterCommit senderId={} receiverId={} tsuntsunId={}",
                            senderId, receiverId, saved.getId());
                    pushTask.run();
                }
            });
        } else {
            log.info("[tsuntsun/send] push immediate senderId={} receiverId={} tsuntsunId={} reason=no_transaction_synchronization",
                    senderId, receiverId, saved.getId());
            pushTask.run();
        }

        return TsunTsunQuizResponse.from(saved, quiz.choices());
    }

    @Transactional
    public TsunTsunAnswerResponse answerTsunTsun(Long tsuntsunId, Long choiceId) {
        TsunTsun tsunTsun = tsunTsunRepository.findWithWordById(tsuntsunId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TsunTsun not found: " + tsuntsunId));
        activityTrackingService.touch(tsunTsun.getReceiver().getId());

        if (tsunTsun.getStatus() == TsunTsunStatus.ANSWERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 답변한 츤츤입니다.");
        }

        Long correctChoiceId = resolveCorrectChoiceId(tsunTsun);
        String correctText = resolveCorrectText(tsunTsun);
        boolean isCorrect = correctChoiceId.equals(choiceId);
        String selectedChoiceText = resolveSelectedChoiceText(tsunTsun.getQuizType(), choiceId);

        tsunTsun.markAnswered();
        tsunTsunAnswerRepository.save(TsunTsunAnswer.of(tsunTsun, selectedChoiceText, isCorrect));
        long pairProgressCount = getPairProgressCount(
                tsunTsun.getSender().getId(),
                tsunTsun.getReceiver().getId(),
                tsunTsun.getTargetDate()
        );
        boolean pairCompletedToday = pairProgressCount >= PAIR_PROGRESS_GOAL;

        if (pairCompletedToday) {
            log.info("[TsunTsun] pair completed today userId={} buddyId={} progress={}/{}",
                    tsunTsun.getSender().getId(),
                    tsunTsun.getReceiver().getId(),
                    pairProgressCount,
                    PAIR_PROGRESS_GOAL);
        }

        return new TsunTsunAnswerResponse(
                tsuntsunId,
                tsunTsun.getQuizType(),
                isCorrect,
                choiceId,
                correctChoiceId,
                correctText,
                pairProgressCount,
                PAIR_PROGRESS_GOAL,
                pairCompletedToday
        );
    }

    private Long resolveCorrectChoiceId(TsunTsun tsunTsun) {
        if (tsunTsun.getQuizType() == TsunTsunQuizType.READING) {
            return tsunTsun.getWord().getId();
        }

        List<Meaning> meanings = meaningRepository.findByWordIdOrderByOrdAsc(tsunTsun.getWord().getId());
        if (meanings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Word has no meaning: " + tsunTsun.getWord().getId());
        }
        return meanings.get(0).getId();
    }

    private String resolveCorrectText(TsunTsun tsunTsun) {
        if (tsunTsun.getQuizType() == TsunTsunQuizType.READING) {
            return tsunTsun.getWord().getReading();
        }

        List<Meaning> meanings = meaningRepository.findByWordIdOrderByOrdAsc(tsunTsun.getWord().getId());
        if (meanings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Word has no meaning: " + tsunTsun.getWord().getId());
        }
        return meanings.get(0).getText();
    }

    private String resolveSelectedChoiceText(TsunTsunQuizType quizType, Long choiceId) {
        if (choiceId.equals(GIVE_UP_CHOICE_ID)) {
            return GIVE_UP_TEXT;
        }

        if (quizType == TsunTsunQuizType.READING) {
            return wordRepository.findById(choiceId)
                    .map(Word::getReading)
                    .orElse("알 수 없는 읽기");
        }

        return meaningRepository.findById(choiceId)
                .map(Meaning::getText)
                .orElse(UNKNOWN_MEANING_TEXT);
    }

    @Transactional(readOnly = true)
    public TsunTsunInboxResponse getInbox(Long userId) {
        activityTrackingService.touch(userId);
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }

        LocalDate today = LocalDate.now(clock);
        List<TsunTsunInboxItemResponse> items = tsunTsunRepository
                .findByReceiverIdAndTargetDateAndStatusOrderByCreatedAtDesc(userId, today, TsunTsunStatus.SENT)
                .stream()
                .map(tsunTsun -> TsunTsunInboxItemResponse.from(
                        tsunTsun,
                        tsunTsunQuizService.generateQuiz(tsunTsun.getWord(), tsunTsun.getQuizType()).choices()
                ))
                .toList();

        return new TsunTsunInboxResponse(userId, items.size(), items);
    }

    @Transactional(readOnly = true)
    public TsunTsunTodayResponse getTodayTsunTsuns(Long userId, Long buddyId) {
        activityTrackingService.touch(userId);
        log.info("[tsuntsun/today] request received: userId={}, buddyId={}", userId, buddyId);

        try {
            if (!userRepository.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
            }
            if (!userRepository.existsById(buddyId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + buddyId);
            }

            boolean connected = hasActiveBuddyRelation(userId, buddyId);
            log.info("[tsuntsun/today] buddy relation check: userId={}, buddyId={}, connected={}", userId, buddyId, connected);
            if (!connected) {
                throw missingBuddyRelation(userId, buddyId);
            }

            LocalDate today = LocalDate.now(clock);
            DailyWordSet buddySet = dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(buddyId, today)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Buddy daily words not found for buddyId=" + buddyId + " on targetDate=" + today
                    ));
            log.info("[tsuntsun/today] buddy daily words found: buddyId={}, targetDate={}, itemCount={}",
                    buddyId, today, buddySet.getItems().size());

            List<TsunTsun> pairTsuns = tsunTsunRepository.findPairByTargetDate(userId, buddyId, today);

            long sentCount = pairTsuns.stream()
                    .filter(t -> t.getSender().getId().equals(userId) && t.getReceiver().getId().equals(buddyId))
                    .count();

            long receivedCount = pairTsuns.stream()
                    .filter(t -> t.getSender().getId().equals(buddyId) && t.getReceiver().getId().equals(userId))
                    .count();
            boolean hasUnreadPetal = pairTsuns.stream()
                    .anyMatch(t -> t.getSender().getId().equals(buddyId)
                            && t.getReceiver().getId().equals(userId)
                            && t.getStatus() == TsunTsunStatus.SENT);
            LocalDateTime lastReceivedAt = pairTsuns.stream()
                    .filter(t -> t.getSender().getId().equals(buddyId) && t.getReceiver().getId().equals(userId))
                    .map(TsunTsun::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            LocalDateTime lastInteractionAt = pairTsuns.stream()
                    .map(TsunTsun::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            long progressCount = getPairProgressCount(userId, buddyId, today);
            boolean pairCompletedToday = progressCount >= PAIR_PROGRESS_GOAL;

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

            TsunTsunTodayResponse response = new TsunTsunTodayResponse(
                    userId,
                    buddyId,
                    today,
                    progressCount,
                    PAIR_PROGRESS_GOAL,
                    sentCount,
                    receivedCount,
                    hasUnreadPetal,
                    lastReceivedAt,
                    lastInteractionAt,
                    pairCompletedToday,
                    items
            );
            log.info("[tsuntsun/today] response ready: userId={}, buddyId={}, progressCount={}, progressGoal={}, sentCount={}, receivedCount={}, pairCompletedToday={}, itemCount={}",
                    userId, buddyId, progressCount, PAIR_PROGRESS_GOAL, sentCount, receivedCount, pairCompletedToday, items.size());
            return response;
        } catch (ResponseStatusException ex) {
            log.warn("[tsuntsun/today] request failed: userId={}, buddyId={}, status={}, reason={}",
                    userId, buddyId, ex.getStatusCode(), ex.getReason());
            throw ex;
        }
    }

    private TsunTsunTodayStatus toTodayStatus(TsunTsunStatus status) {
        return switch (status) {
            case SENT -> TsunTsunTodayStatus.SENT;
            case ANSWERED -> TsunTsunTodayStatus.ANSWERED;
        };
    }

    private long getPairProgressCount(Long userId, Long buddyId, LocalDate targetDate) {
        long leftUserId = Math.min(userId, buddyId);
        long rightUserId = Math.max(userId, buddyId);

        long leftToRightAnsweredCount = tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(
                leftUserId,
                rightUserId,
                targetDate,
                TsunTsunStatus.ANSWERED
        );
        long rightToLeftAnsweredCount = tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(
                rightUserId,
                leftUserId,
                targetDate,
                TsunTsunStatus.ANSWERED
        );
        long progressCount = Math.min(leftToRightAnsweredCount, rightToLeftAnsweredCount);

        log.info("[tsuntsun/progress] pair progress calculated: leftUserId={}, rightUserId={}, leftToRightAnsweredCount={}, rightToLeftAnsweredCount={}, progressCount={}",
                leftUserId, rightUserId, leftToRightAnsweredCount, rightToLeftAnsweredCount, progressCount);
        return progressCount;
    }

    private void validateBuddyRelation(Buddy buddy, Long buddyId, Long userId) {
        boolean reverseConnected = buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(buddyId, userId, BuddyStatus.ACTIVE);
        if (buddy == null || !reverseConnected) {
            throw missingBuddyRelation(userId, buddyId);
        }
    }

    private Buddy getActiveBuddy(Long userId, Long buddyId) {
        return buddyRepository.findByUserIdAndBuddyUserIdAndStatus(userId, buddyId, BuddyStatus.ACTIVE)
                .orElseThrow(() -> missingBuddyRelation(userId, buddyId));
    }

    private boolean hasActiveBuddyRelation(Long userId, Long buddyId) {
        return buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(userId, buddyId, BuddyStatus.ACTIVE)
                && buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(buddyId, userId, BuddyStatus.ACTIVE);
    }

    private ResponseStatusException missingBuddyRelation(Long userId, Long buddyId) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Buddy relationship not found between userId=" + userId + " and buddyId=" + buddyId
        );
    }
}
