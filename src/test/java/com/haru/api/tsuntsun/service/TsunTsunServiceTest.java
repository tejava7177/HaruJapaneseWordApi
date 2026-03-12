package com.haru.api.tsuntsun.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.dailyword.domain.DailyWordSet;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.push.ApnsPushService;
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
import com.haru.api.word.domain.Meaning;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.MeaningRepository;
import java.time.LocalDate;
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

    @Mock private TsunTsunRepository tsunTsunRepository;
    @Mock private TsunTsunAnswerRepository tsunTsunAnswerRepository;
    @Mock private UserRepository userRepository;
    @Mock private BuddyRepository buddyRepository;
    @Mock private DailyWordItemRepository dailyWordItemRepository;
    @Mock private DailyWordSetRepository dailyWordSetRepository;
    @Mock private MeaningRepository meaningRepository;
    @Mock private TsunTsunQuizService tsunTsunQuizService;
    @Mock private ApnsPushService apnsPushService;

    private TsunTsunService tsunTsunService;

    @BeforeEach
    void setUp() {
        tsunTsunService = new TsunTsunService(
                tsunTsunRepository,
                tsunTsunAnswerRepository,
                userRepository,
                buddyRepository,
                dailyWordItemRepository,
                dailyWordSetRepository,
                meaningRepository,
                tsunTsunQuizService,
                apnsPushService
        );
    }

    @Test
    void sendTsunTsun_failsWhenPendingExists() {
        LocalDate today = LocalDate.now();
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(true);

        assertThatThrownBy(() -> tsunTsunService.sendTsunTsun(1L, 2L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이전 츤츤에 답하지 않았습니다");
    }

    @Test
    void sendTsunTsun_failsWhenSameDailyWordItemAlreadySent() {
        LocalDate today = LocalDate.now();
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
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
        LocalDate today = LocalDate.now();
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User buddyB = new User(2L, "b", WordLevel.N4, "BBBB2222");
        User buddyC = new User(3L, "c", WordLevel.N4, "CCCC3333");

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(buddyB));
        given(userRepository.findById(3L)).willReturn(Optional.of(buddyC));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 3L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(3L, 1L, BuddyStatus.ACTIVE)).willReturn(true);

        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(10L);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 3L, today)).willReturn(0L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 3L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(1L, 3L, 12L, today)).willReturn(false);

        DailyWordItem item = mockDailyWordItem(12L, buddyC, today);
        given(dailyWordItemRepository.findById(12L)).willReturn(Optional.of(item));

        Word word = item.getWord();
        TsunTsun saved = TsunTsun.sent(sender, buddyC, word, item, today);
        ReflectionTestUtils.setField(saved, "id", 99L);
        given(tsunTsunRepository.save(ArgumentMatchers.any(TsunTsun.class))).willReturn(saved);
        given(tsunTsunQuizService.generateChoices(word)).willReturn(List.of(new QuizChoiceResponse(1L, "뜻")));

        assertThatThrownBy(() -> tsunTsunService.sendTsunTsun(1L, 2L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("하루 최대 10회");

        TsunTsunQuizResponse response = tsunTsunService.sendTsunTsun(1L, 3L, 12L);
        assertThat(response.tsuntsunId()).isEqualTo(99L);
    }

    @Test
    void answerThenSendAgain_isAllowed() {
        LocalDate today = LocalDate.now();
        User sender = new User(1L, "s", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "r", WordLevel.N4, "BBBB2222");

        Word word = new Word("ああ", "ああ", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 100L);

        Meaning correct = new Meaning(word, "정답", 1);
        Meaning wrong = new Meaning(word, "오답", 2);
        ReflectionTestUtils.setField(correct, "id", 501L);
        ReflectionTestUtils.setField(wrong, "id", 502L);

        DailyWordItem item = mockDailyWordItem(11L, receiver, today);

        TsunTsun tsun = TsunTsun.sent(sender, receiver, word, item, today);
        given(tsunTsunRepository.findWithWordById(1L)).willReturn(Optional.of(tsun));
        given(meaningRepository.findByWordIdOrderByOrdAsc(100L)).willReturn(List.of(correct, wrong));

        TsunTsunAnswerResponse answer = tsunTsunService.answerTsunTsun(1L, 501L);
        assertThat(answer.correct()).isTrue();
        assertThat(answer.selectedMeaningId()).isEqualTo(501L);
        assertThat(answer.correctText()).isEqualTo("정답");
        assertThat(answer.status()).isEqualTo(TsunTsunStatus.ANSWERED);

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(true);
        given(tsunTsunRepository.countBySenderIdAndReceiverIdAndTargetDate(1L, 2L, today)).willReturn(1L);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndTargetDateAndStatus(1L, 2L, today, TsunTsunStatus.SENT)).willReturn(false);
        given(tsunTsunRepository.existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(1L, 2L, 11L, today)).willReturn(false);
        given(dailyWordItemRepository.findById(11L)).willReturn(Optional.of(item));
        Word sendWord = item.getWord();
        TsunTsun saved = TsunTsun.sent(sender, receiver, sendWord, item, today);
        ReflectionTestUtils.setField(saved, "id", 88L);
        given(tsunTsunRepository.save(ArgumentMatchers.any(TsunTsun.class))).willReturn(saved);
        given(tsunTsunQuizService.generateChoices(sendWord)).willReturn(List.of(new QuizChoiceResponse(501L, "정답")));

        TsunTsunQuizResponse next = tsunTsunService.sendTsunTsun(1L, 2L, 11L);
        assertThat(next.tsuntsunId()).isEqualTo(88L);
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
        LocalDate today = LocalDate.now();
        User sender = new User(1L, "김민성", WordLevel.N4, "AAAA1111");
        User receiver = new User(2L, "buddy2", WordLevel.N4, "BBBB2222");
        Word word = new Word("紹介", "しょうかい", WordLevel.N4);
        ReflectionTestUtils.setField(word, "id", 390L);

        DailyWordItem item = mock(DailyWordItem.class);
        given(userRepository.existsById(2L)).willReturn(true);

        TsunTsun tsunTsun = TsunTsun.sent(sender, receiver, word, item, today);
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
}
