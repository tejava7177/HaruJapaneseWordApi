package com.haru.api.tsuntsun.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.push.PushNotificationService;
import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunInboxResponse;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.user.service.ActivityTrackingService;
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.MeaningRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TsunTsunServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 3, 28);

    @Mock private TsunTsunRepository tsunTsunRepository;
    @Mock private TsunTsunAnswerRepository tsunTsunAnswerRepository;
    @Mock private UserRepository userRepository;
    @Mock private BuddyRepository buddyRepository;
    @Mock private DailyWordItemRepository dailyWordItemRepository;
    @Mock private DailyWordSetRepository dailyWordSetRepository;
    @Mock private MeaningRepository meaningRepository;
    @Mock private TsunTsunQuizService tsunTsunQuizService;
    @Mock private PushNotificationService pushNotificationService;
    @Mock private ActivityTrackingService activityTrackingService;

    private Clock clock;
    private TsunTsunService tsunTsunService;

    @BeforeEach
    void setUp() {
        clock = fixedClockAtKst("2026-03-28T00:00:00+09:00");
        tsunTsunService = new TsunTsunService(
                tsunTsunRepository,
                tsunTsunAnswerRepository,
                userRepository,
                buddyRepository,
                dailyWordItemRepository,
                dailyWordSetRepository,
                meaningRepository,
                tsunTsunQuizService,
                pushNotificationService,
                activityTrackingService,
                clock
        );
    }

    @Test
    void sendTsunTsun_failsWhenSenderAlreadyHasPendingTsunTsun() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(sender, receiver, 10L)));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(true);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.SENT)).willReturn(false);

        assertThatThrownBy(() -> tsunTsunService.sendTsunTsun(1L, 2L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이전 츤츤에 답하지 않았습니다");
    }

    @Test
    void sendTsunTsun_failsWhenReceiverHasUnansweredTsunTsunForSender() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(2L, "b", WordLevel.N4, "BBBB2222");
        User receiver = new User(1L, "a", WordLevel.N4, "AAAA1111");

        given(userRepository.findById(2L)).willReturn(Optional.of(sender));
        given(userRepository.findById(1L)).willReturn(Optional.of(receiver));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(sender, receiver, 10L)));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(2L, 1L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(true);

        assertThatThrownBy(() -> tsunTsunService.sendTsunTsun(2L, 1L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("먼저 받은 츤츤에 답해주세요. 답변 후 새 츤츤을 보낼 수 있어요.");
    }

    @Test
    void sendTsunTsun_failsWhenSameDailyWordItemAlreadySent() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(sender, receiver, 10L)));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(1L, 2L, 11L, today)).willReturn(true);

        assertThatThrownBy(() -> tsunTsunService.sendTsunTsun(1L, 2L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("같은 단어는 재전송");
    }

    @Test
    void sendTsunTsun_pairLimitWorksSeparatelyByBuddy() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User buddyB = new User(2L, "b", WordLevel.N4, "BBBB2222");
        User buddyC = new User(3L, "c", WordLevel.N4, "CCCC3333");

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(buddyB));
        given(userRepository.findById(3L)).willReturn(Optional.of(buddyC));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(sender, buddyB, 20L)));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 3L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(sender, buddyC, 30L)));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(3L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(10L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 3L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 3L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(3L, 1L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(1L, 3L, 12L, today)).willReturn(false);

        DailyWordItem item = mockDailyWordItem(12L, buddyC, today);
        given(dailyWordItemRepository.findById(12L)).willReturn(Optional.of(item));

        Word word = item.getWord();
        TsunTsun saved = testTsunTsun(sender, buddyC, word, item, 30L, today);
        ReflectionTestUtils.setField(saved, "id", 99L);
        given(tsunTsunRepository.saveAndFlush(ArgumentMatchers.any(TsunTsun.class))).willReturn(saved);
        given(tsunTsunQuizService.generateChoices(word)).willReturn(List.of(new QuizChoiceResponse(1L, "뜻")));

        assertThatThrownBy(() -> tsunTsunService.sendTsunTsun(1L, 2L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("하루 최대 10회");

        TsunTsunQuizResponse response = tsunTsunService.sendTsunTsun(1L, 3L, 12L);
        assertThat(response.tsuntsunId()).isEqualTo(99L);
    }

    @Test
    void answerThenSendAgain_isAllowed() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        Word word = new Word("ああ", "ああ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 100L);

        Meaning correct = new Meaning(word, "정답", 1);
        Meaning wrong = new Meaning(word, "오답", 2);
        ReflectionTestUtils.setField(correct, "id", 501L);
        ReflectionTestUtils.setField(wrong, "id", 502L);

        DailyWordItem item = mockDailyWordItem(11L, receiver, today);

        TsunTsun tsun = testTsunTsun(sender, receiver, word, item, 10L, today);
        given(tsunTsunRepository.findWithWordById(1L)).willReturn(Optional.of(tsun));
        given(meaningRepository.findByWordIdOrderByOrdAsc(100L)).willReturn(List.of(correct, wrong));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(0L);

        TsunTsunAnswerResponse answer = tsunTsunService.answerTsunTsun(1L, 501L);
        assertThat(answer.correct()).isTrue();
        assertThat(answer.selectedMeaningId()).isEqualTo(501L);
        assertThat(answer.correctMeaningId()).isEqualTo(501L);
        assertThat(answer.correctText()).isEqualTo("정답");
        assertThat(answer.pairProgressCount()).isEqualTo(0L);
        assertThat(answer.pairProgressGoal()).isEqualTo(10L);
        assertThat(answer.pairCompletedToday()).isFalse();

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(sender, receiver, 10L)));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(1L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(1L, 2L, 11L, today)).willReturn(false);
        given(dailyWordItemRepository.findById(11L)).willReturn(Optional.of(item));
        Word sendWord = item.getWord();
        TsunTsun saved = testTsunTsun(sender, receiver, sendWord, item, 10L, today);
        ReflectionTestUtils.setField(saved, "id", 88L);
        given(tsunTsunRepository.saveAndFlush(ArgumentMatchers.any(TsunTsun.class))).willReturn(saved);
        given(tsunTsunQuizService.generateChoices(sendWord)).willReturn(List.of(new QuizChoiceResponse(501L, "정답")));

        TsunTsunQuizResponse next = tsunTsunService.sendTsunTsun(1L, 2L, 11L);
        assertThat(next.tsuntsunId()).isEqualTo(88L);
        verify(pushNotificationService).notifyTsunTsunReceived(2L, 88L, 1L, "s");
    }

    @Test
    void sendTsunTsun_isAllowedInReverseDirectionAfterAnswer() {
        LocalDate today = FIXED_TODAY;
        User userA = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User userB = new User(2L, "b", WordLevel.N4, "BBBB2222");

        Word receivedWord = new Word("ああ", "ああ", WordLevel.N4);
        ReflectionTestUtils.setField(receivedWord, "id", 100L);
        Meaning correct = new Meaning(receivedWord, "정답", 1);
        ReflectionTestUtils.setField(correct, "id", 501L);

        DailyWordItem receivedItem = mock(DailyWordItem.class);
        TsunTsun receivedTsun = testTsunTsun(userA, userB, receivedWord, receivedItem, 10L, today);

        given(tsunTsunRepository.findWithWordById(10L)).willReturn(Optional.of(receivedTsun));
        given(meaningRepository.findByWordIdOrderByOrdAsc(100L)).willReturn(List.of(correct));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(0L);

        TsunTsunAnswerResponse answer = tsunTsunService.answerTsunTsun(10L, 501L);
        assertThat(answer.correct()).isTrue();

        DailyWordItem sendItem = mockDailyWordItem(12L, userA, today);
        Word sendWord = sendItem.getWord();

        given(userRepository.findById(2L)).willReturn(Optional.of(userB));
        given(userRepository.findById(1L)).willReturn(Optional.of(userA));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE))
                .willReturn(Optional.of(mockActiveBuddy(userB, userA, 10L)));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(2L, 1L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(2L, 1L, 12L, today)).willReturn(false);
        given(dailyWordItemRepository.findById(12L)).willReturn(Optional.of(sendItem));

        TsunTsun saved = testTsunTsun(userB, userA, sendWord, sendItem, 10L, today);
        ReflectionTestUtils.setField(saved, "id", 77L);
        given(tsunTsunRepository.saveAndFlush(ArgumentMatchers.any(TsunTsun.class))).willReturn(saved);
        given(tsunTsunQuizService.generateChoices(sendWord)).willReturn(List.of(new QuizChoiceResponse(601L, "뜻")));

        TsunTsunQuizResponse response = tsunTsunService.sendTsunTsun(2L, 1L, 12L);

        assertThat(response.tsuntsunId()).isEqualTo(77L);
        verify(pushNotificationService).notifyTsunTsunReceived(1L, 77L, 2L, "b");
    }

    private Buddy mockActiveBuddy(User user, User buddyUser, Long relationshipId) {
        BuddyRelationship relationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(relationship, "id", relationshipId);
        Buddy buddy = Buddy.active(user, buddyUser, relationship);
        ReflectionTestUtils.setField(buddy, "id", relationshipId + 100L);
        return buddy;
    }

    private TsunTsun testTsunTsun(
            User sender,
            User receiver,
            Word word,
            DailyWordItem item,
            Long relationshipId,
            LocalDate targetDate
    ) {
        BuddyRelationship relationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(relationship, "id", relationshipId);
        TsunTsun tsunTsun = TsunTsun.sent(sender, receiver, word, item, relationship, targetDate);
        ReflectionTestUtils.setField(tsunTsun, "createdAt", LocalDateTime.of(targetDate, java.time.LocalTime.NOON));
        return tsunTsun;
    }

    @Test
    void answerTsunTsun_returnsWrongResultForWrongChoiceFromAnotherWord() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        Word word = new Word("ああ", "ああ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 100L);

        Meaning correct = new Meaning(word, "정답", 1);
        ReflectionTestUtils.setField(correct, "id", 501L);

        DailyWordItem item = mock(DailyWordItem.class);
        TsunTsun tsun = testTsunTsun(sender, receiver, word, item, 10L, today);

        Word wrongWord = new Word("べつ", "べつ", WordLevel.N4);
        Meaning wrong = new Meaning(wrongWord, "오답", 1);
        ReflectionTestUtils.setField(wrong, "id", 502L);

        given(tsunTsunRepository.findWithWordById(1L)).willReturn(Optional.of(tsun));
        given(meaningRepository.findByWordIdOrderByOrdAsc(100L)).willReturn(List.of(correct));
        given(meaningRepository.findById(502L)).willReturn(Optional.of(wrong));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(4L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);

        TsunTsunAnswerResponse answer = tsunTsunService.answerTsunTsun(1L, 502L);

        assertThat(answer.tsuntsunId()).isEqualTo(1L);
        assertThat(answer.correct()).isFalse();
        assertThat(answer.selectedMeaningId()).isEqualTo(502L);
        assertThat(answer.correctMeaningId()).isEqualTo(501L);
        assertThat(answer.correctText()).isEqualTo("정답");
        assertThat(answer.pairProgressCount()).isEqualTo(1L);
        assertThat(answer.pairCompletedToday()).isFalse();
        assertThat(tsun.getStatus()).isEqualTo(TsunTsunStatus.ANSWERED);
        verify(tsunTsunAnswerRepository).save(ArgumentMatchers.any());
    }

    @Test
    void answerTsunTsun_returnsWrongResultForGiveUpChoice() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        Word word = new Word("ああ", "ああ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 100L);

        Meaning correct = new Meaning(word, "정답", 1);
        ReflectionTestUtils.setField(correct, "id", 501L);

        DailyWordItem item = mock(DailyWordItem.class);
        TsunTsun tsun = testTsunTsun(sender, receiver, word, item, 10L, today);

        given(tsunTsunRepository.findWithWordById(1L)).willReturn(Optional.of(tsun));
        given(meaningRepository.findByWordIdOrderByOrdAsc(100L)).willReturn(List.of(correct));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(10L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(10L);

        TsunTsunAnswerResponse answer = tsunTsunService.answerTsunTsun(1L, -1L);

        assertThat(answer.correct()).isFalse();
        assertThat(answer.selectedMeaningId()).isEqualTo(-1L);
        assertThat(answer.correctMeaningId()).isEqualTo(501L);
        assertThat(answer.correctText()).isEqualTo("정답");
        assertThat(answer.pairProgressCount()).isEqualTo(10L);
        assertThat(answer.pairCompletedToday()).isTrue();
    }

    @Test
    void answerTsunTsun_failsWhenAlreadyAnswered() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        Word word = new Word("ああ", "ああ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 100L);

        DailyWordItem item = mock(DailyWordItem.class);
        TsunTsun tsun = testTsunTsun(sender, receiver, word, item, 10L, today);
        tsun.markAnswered();

        given(tsunTsunRepository.findWithWordById(1L)).willReturn(Optional.of(tsun));

        assertThatThrownBy(() -> tsunTsunService.answerTsunTsun(1L, 501L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이미 답변한 츤츤입니다.");
    }

    @Test
    void getTodayTsunTsuns_failsWithClearMessageWhenBuddyRelationMissing() {
        given(userRepository.existsById(4L)).willReturn(true);
        given(userRepository.existsById(3L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(4L, 3L, BuddyStatus.ACTIVE)).willReturn(false);

        assertThatThrownBy(() -> tsunTsunService.getTodayTsunTsuns(4L, 3L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Buddy relationship not found between userId=4 and buddyId=3");
    }

    @Test
    void getInbox_returnsTodayUnansweredTsunsForReceiver() {
        LocalDate today = FIXED_TODAY;
        User sender = new User(1L, "김민성", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "buddy2", WordLevel.N4, "BBBB2222");
        Word word = new Word("紹介", "しょうかい", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 390L);

        DailyWordItem item = mock(DailyWordItem.class);
        given(userRepository.existsById(2L)).willReturn(true);

        TsunTsun tsunTsun = testTsunTsun(sender, receiver, word, item, 10L, today);
        ReflectionTestUtils.setField(tsunTsun, "id", 11L);
        given(tsunTsunRepository.findByReceiverIdAndTargetDateAndStatusOrderByCreatedAtDesc(2L, today, TsunTsunStatus.SENT))
                .willReturn(List.of(tsunTsun));
        given(tsunTsunQuizService.generateChoices(word))
                .willReturn(List.of(new QuizChoiceResponse(100L, "소개"), new QuizChoiceResponse(-1L, "모르겠어요")));

        TsunTsunInboxResponse response = tsunTsunService.getInbox(2L);

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.unansweredCount()).isEqualTo(1);
        assertThat(response.items().get(0).senderName()).isEqualTo("김민성");
        assertThat(response.items().get(0).choices()).hasSize(2);
    }

    @Test
    void getInbox_failsWhenUserMissing() {
        given(userRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> tsunTsunService.getInbox(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 999");
    }

    @Test
    void getTodayTsunTsuns_returnsAnsweredCountAsProgress() {
        LocalDate today = FIXED_TODAY;
        User user = new User(1L, "u", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");
        Word word = new Word("あ", "あ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 1021L);

        given(userRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        DailyWordSet buddySet = mock(DailyWordSet.class);
        DailyWordItem buddyItem = mock(DailyWordItem.class);
        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(2L, today)).willReturn(Optional.of(buddySet));
        given(buddySet.getItems()).willReturn(List.of(buddyItem));
        given(buddyItem.getId()).willReturn(21L);
        given(buddyItem.getWord()).willReturn(word);

        TsunTsun answered = testTsunTsun(user, buddy, word, buddyItem, 10L, today);
        answered.markAnswered();
        ReflectionTestUtils.setField(answered, "id", 31L);
        given(tsunTsunRepository.findPairByTargetDate(1L, 2L, today)).willReturn(List.of(answered));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(0L);

        var response = tsunTsunService.getTodayTsunTsuns(1L, 2L);

        assertThat(response.progressCount()).isEqualTo(0L);
        assertThat(response.progressGoal()).isEqualTo(10L);
        assertThat(response.pairCompletedToday()).isFalse();
        assertThat(response.sentCount()).isEqualTo(1L);
        assertThat(response.receivedCount()).isEqualTo(0L);
        assertThat(response.hasUnreadPetal()).isFalse();
    }

    @Test
    void getTodayTsunTsuns_sendOnlyDoesNotIncreaseProgress() {
        LocalDate today = FIXED_TODAY;
        User user = new User(1L, "u", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");
        Word word = new Word("あ", "あ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 1031L);

        given(userRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        DailyWordSet buddySet = mock(DailyWordSet.class);
        DailyWordItem buddyItem = mock(DailyWordItem.class);
        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(2L, today)).willReturn(Optional.of(buddySet));
        given(buddySet.getItems()).willReturn(List.of(buddyItem));
        given(buddyItem.getId()).willReturn(31L);
        given(buddyItem.getWord()).willReturn(word);

        TsunTsun sentOnly = testTsunTsun(user, buddy, word, buddyItem, 10L, today);
        ReflectionTestUtils.setField(sentOnly, "id", 41L);
        given(tsunTsunRepository.findPairByTargetDate(1L, 2L, today)).willReturn(List.of(sentOnly));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(0L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(0L);

        var response = tsunTsunService.getTodayTsunTsuns(1L, 2L);

        assertThat(response.progressCount()).isEqualTo(0L);
        assertThat(response.pairCompletedToday()).isFalse();
        assertThat(response.sentCount()).isEqualTo(1L);
        assertThat(response.receivedCount()).isEqualTo(0L);
        assertThat(response.hasUnreadPetal()).isFalse();
    }

    @Test
    void getTodayTsunTsuns_countsRoundTripsUsingMinOfDirectionalAnsweredCounts() {
        LocalDate today = FIXED_TODAY;
        User user = new User(1L, "u", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");

        given(userRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        DailyWordSet buddySet = mock(DailyWordSet.class);
        DailyWordItem buddyItem1 = mock(DailyWordItem.class);
        DailyWordItem buddyItem2 = mock(DailyWordItem.class);
        Word word1 = new Word("あ", "あ", WordLevel.N4);
        Word word2 = new Word("い", "い", WordLevel.N4);
        ReflectionTestUtils.setField(word1, "id", 1041L);
        ReflectionTestUtils.setField(word2, "id", 1042L);

        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(2L, today)).willReturn(Optional.of(buddySet));
        given(buddySet.getItems()).willReturn(List.of(buddyItem1, buddyItem2));
        given(buddyItem1.getId()).willReturn(41L);
        given(buddyItem1.getWord()).willReturn(word1);
        given(buddyItem2.getId()).willReturn(42L);
        given(buddyItem2.getWord()).willReturn(word2);

        TsunTsun answeredSent = testTsunTsun(user, buddy, word1, buddyItem1, 10L, today);
        answeredSent.markAnswered();
        TsunTsun answeredReceived = testTsunTsun(buddy, user, word2, buddyItem2, 10L, today);
        answeredReceived.markAnswered();
        TsunTsun pendingReceived = testTsunTsun(buddy, user, word1, buddyItem1, 10L, today);

        given(tsunTsunRepository.findPairByTargetDate(1L, 2L, today))
                .willReturn(List.of(answeredSent, answeredReceived, pendingReceived));
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);

        var response = tsunTsunService.getTodayTsunTsuns(1L, 2L);

        assertThat(response.progressCount()).isEqualTo(1L);
        assertThat(response.pairCompletedToday()).isFalse();
        assertThat(response.sentCount()).isEqualTo(1L);
        assertThat(response.receivedCount()).isEqualTo(2L);
    }

    @Test
    void getTodayTsunTsuns_progressUsesMinimumWhenAnsweredCountsAreUnbalanced() {
        LocalDate today = FIXED_TODAY;
        given(userRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        DailyWordSet buddySet = mock(DailyWordSet.class);
        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(2L, today)).willReturn(Optional.of(buddySet));
        given(buddySet.getItems()).willReturn(List.of());
        given(tsunTsunRepository.findPairByTargetDate(1L, 2L, today)).willReturn(List.of());
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(2L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);

        var response = tsunTsunService.getTodayTsunTsuns(1L, 2L);

        assertThat(response.progressCount()).isEqualTo(1L);
    }

    @Test
    void getTodayTsunTsuns_progressIsSameWhenUserOrderIsReversed() {
        LocalDate today = FIXED_TODAY;
        given(userRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        DailyWordSet user1Set = mock(DailyWordSet.class);
        DailyWordSet user2Set = mock(DailyWordSet.class);
        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(2L, today)).willReturn(Optional.of(user2Set));
        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(1L, today)).willReturn(Optional.of(user1Set));
        given(user1Set.getItems()).willReturn(List.of());
        given(user2Set.getItems()).willReturn(List.of());
        given(tsunTsunRepository.findPairByTargetDate(1L, 2L, today)).willReturn(List.of());
        given(tsunTsunRepository.findPairByTargetDate(2L, 1L, today)).willReturn(List.of());
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.ANSWERED)).willReturn(2L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, today, TsunTsunStatus.ANSWERED)).willReturn(1L);

        var responseA = tsunTsunService.getTodayTsunTsuns(1L, 2L);
        var responseB = tsunTsunService.getTodayTsunTsuns(2L, 1L);

        assertThat(responseA.progressCount()).isEqualTo(1L);
        assertThat(responseB.progressCount()).isEqualTo(1L);
    }

    @Test
    void getInbox_usesKstDateBeforeMidnight() {
        TsunTsunService service = new TsunTsunService(
                tsunTsunRepository,
                tsunTsunAnswerRepository,
                userRepository,
                buddyRepository,
                dailyWordItemRepository,
                dailyWordSetRepository,
                meaningRepository,
                tsunTsunQuizService,
                pushNotificationService,
                activityTrackingService,
                fixedClockAtKst("2026-03-27T23:59:00+09:00")
        );
        LocalDate targetDate = LocalDate.of(2026, 3, 27);

        given(userRepository.existsById(2L)).willReturn(true);
        given(tsunTsunRepository.findByReceiverIdAndTargetDateAndStatusOrderByCreatedAtDesc(2L, targetDate, TsunTsunStatus.SENT))
                .willReturn(List.of());

        TsunTsunInboxResponse response = service.getInbox(2L);

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.unansweredCount()).isEqualTo(0);
    }

    @Test
    void getTodayTsunTsuns_usesKstDateAfterMidnight() {
        TsunTsunService service = new TsunTsunService(
                tsunTsunRepository,
                tsunTsunAnswerRepository,
                userRepository,
                buddyRepository,
                dailyWordItemRepository,
                dailyWordSetRepository,
                meaningRepository,
                tsunTsunQuizService,
                pushNotificationService,
                activityTrackingService,
                fixedClockAtKst("2026-03-28T00:00:00+09:00")
        );
        LocalDate targetDate = LocalDate.of(2026, 3, 28);

        given(userRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        DailyWordSet buddySet = mock(DailyWordSet.class);
        given(dailyWordSetRepository.findWithItemsByUserIdAndTargetDate(2L, targetDate)).willReturn(Optional.of(buddySet));
        given(buddySet.getItems()).willReturn(List.of());
        given(tsunTsunRepository.findPairByTargetDate(1L, 2L, targetDate)).willReturn(List.of());
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, targetDate, TsunTsunStatus.ANSWERED)).willReturn(0L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDateAndStatus(2L, 1L, targetDate, TsunTsunStatus.ANSWERED)).willReturn(0L);

        var response = service.getTodayTsunTsuns(1L, 2L);

        assertThat(response.targetDate()).isEqualTo(targetDate);
    }

    private DailyWordItem mockDailyWordItem(Long id, User receiver, LocalDate targetDate) {
        DailyWordItem item = mock(DailyWordItem.class);
        DailyWordSet set = mock(DailyWordSet.class);
        Word word = new Word("あ", "あ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 1000L + id);

        given(item.getDailyWordSet()).willReturn(set);
        given(item.getWord()).willReturn(word);
        given(set.getUser()).willReturn(receiver);
        given(set.getTargetDate()).willReturn(targetDate);
        return item;
    }

    private Clock fixedClockAtKst(String isoOffsetDateTime) {
        return Clock.fixed(OffsetDateTime.parse(isoOffsetDateTime).toInstant(), KST);
    }
}
