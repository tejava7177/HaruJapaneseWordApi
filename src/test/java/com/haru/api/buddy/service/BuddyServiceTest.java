package com.haru.api.buddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.dto.BuddyResponse;
import com.haru.api.buddy.dto.DeleteBuddyResponse;
import com.haru.api.buddy.dto.RandomMatchingCandidateResponse;
import com.haru.api.buddy.repository.BuddyRequestRepository;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BuddyServiceTest {

    @Mock
    private BuddyRelationshipRepository buddyRelationshipRepository;

    @Mock
    private BuddyRepository buddyRepository;

    @Mock
    private BuddyRequestRepository buddyRequestRepository;

    @Mock
    private TsunTsunRepository tsunTsunRepository;

    @Mock
    private UserRepository userRepository;

    private BuddyService buddyService;

    @BeforeEach
    void setUp() {
        buddyService = new BuddyService(
                buddyRelationshipRepository,
                buddyRepository,
                buddyRequestRepository,
                tsunTsunRepository,
                userRepository
        );
    }

    @Test
    void connectByBuddyCode_success() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findByBuddyCode("BBBB2222")).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(2L, BuddyStatus.ACTIVE)).willReturn(0L);
        BuddyRelationship relationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(relationship, "id", 10L);
        given(buddyRelationshipRepository.saveAndFlush(any(BuddyRelationship.class))).willReturn(relationship);
        given(buddyRepository.saveAllAndFlush(any())).willAnswer(invocation -> invocation.getArgument(0));

        BuddyResponse response = buddyService.connectByBuddyCode(1L, "BBBB2222");

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.buddyUserId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(BuddyStatus.ACTIVE);
        assertThat(response.tikiTakaCount()).isEqualTo(0L);
    }

    @Test
    void connectThenList_returnsConnectedBuddy() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");
        BuddyRelationship relationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(relationship, "id", 10L);
        Buddy userToBuddy = Buddy.active(user, buddy, relationship);
        ReflectionTestUtils.setField(userToBuddy, "id", 100L);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findByBuddyCode("BBBB2222")).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(2L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRelationshipRepository.saveAndFlush(any(BuddyRelationship.class))).willReturn(relationship);
        given(buddyRepository.saveAllAndFlush(any())).willReturn(List.of(userToBuddy));
        given(userRepository.existsById(1L)).willReturn(true);
        given(buddyRepository.findByUserIdAndStatusOrderByCreatedAtAsc(1L, BuddyStatus.ACTIVE)).willReturn(List.of(userToBuddy));
        given(tsunTsunRepository.countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
                10L, 1L, 2L, TsunTsunStatus.ANSWERED)).willReturn(0L);
        given(tsunTsunRepository.countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
                10L, 2L, 1L, TsunTsunStatus.ANSWERED)).willReturn(0L);

        buddyService.connectByBuddyCode(1L, "BBBB2222");
        List<BuddyResponse> responses = buddyService.getBuddies(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).buddyUserId()).isEqualTo(2L);
    }

    @Test
    void connectByBuddyCode_failsWhenOverMaxBuddy() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findByBuddyCode("BBBB2222")).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(3L);

        assertThatThrownBy(() -> buddyService.connectByBuddyCode(1L, "BBBB2222"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("최대 3명의 버디");
    }

    @Test
    void connectByBuddyCode_failsWhenAlreadyConnected() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findByBuddyCode("BBBB2222")).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(true);

        assertThatThrownBy(() -> buddyService.connectByBuddyCode(1L, "BBBB2222"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이미 연결된 버디");
    }

    @Test
    void getBuddies_returnsTikiTakaCountUsingMinAnsweredCountPerRelationship() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N3, "BBBB2222");
        BuddyRelationship relationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(relationship, "id", 30L);
        Buddy userToBuddy = Buddy.active(user, buddy, relationship);
        ReflectionTestUtils.setField(userToBuddy, "id", 100L);

        given(userRepository.existsById(1L)).willReturn(true);
        given(buddyRepository.findByUserIdAndStatusOrderByCreatedAtAsc(1L, BuddyStatus.ACTIVE))
                .willReturn(List.of(userToBuddy));
        given(tsunTsunRepository.countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
                30L, 1L, 2L, TsunTsunStatus.ANSWERED)).willReturn(5L);
        given(tsunTsunRepository.countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
                30L, 2L, 1L, TsunTsunStatus.ANSWERED)).willReturn(3L);

        List<BuddyResponse> responses = buddyService.getBuddies(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).tikiTakaCount()).isEqualTo(3L);
    }

    @Test
    void removeBuddy_deletesBothDirections() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");
        BuddyRelationship relationship = BuddyRelationship.create();
        Buddy userToBuddy = Buddy.active(user, buddy, relationship);
        Buddy buddyToUser = Buddy.active(buddy, user, relationship);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.of(userToBuddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.of(buddyToUser));

        DeleteBuddyResponse response = buddyService.removeBuddy(1L, 2L);

        verify(buddyRepository).deleteAll(List.of(userToBuddy, buddyToUser));
        assertThat(response.deleted()).isTrue();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.buddyUserId()).isEqualTo(2L);
    }

    @Test
    void removeBuddy_thenListDoesNotContainBuddy() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");
        BuddyRelationship relationship = BuddyRelationship.create();
        Buddy userToBuddy = Buddy.active(user, buddy, relationship);
        Buddy buddyToUser = Buddy.active(buddy, user, relationship);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.of(userToBuddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.of(buddyToUser));
        given(userRepository.existsById(1L)).willReturn(true);
        given(buddyRepository.findByUserIdAndStatusOrderByCreatedAtAsc(1L, BuddyStatus.ACTIVE)).willReturn(List.of());

        buddyService.removeBuddy(1L, 2L);
        List<BuddyResponse> responses = buddyService.getBuddies(1L);

        assertThat(responses).isEmpty();
    }

    @Test
    void removeBuddy_failsWhenBuddyDoesNotExist() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> buddyService.removeBuddy(1L, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("활성 버디 관계가 없습니다");
    }

    @Test
    void removeBuddy_thenReconnectIsPossible() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User buddy = new User(2L, "b", WordLevel.N4, "BBBB2222");
        BuddyRelationship oldRelationship = BuddyRelationship.create();
        Buddy userToBuddy = Buddy.active(user, buddy, oldRelationship);
        Buddy buddyToUser = Buddy.active(buddy, user, oldRelationship);
        BuddyRelationship newRelationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(newRelationship, "id", 20L);
        Buddy reconnected = Buddy.active(user, buddy, newRelationship);
        ReflectionTestUtils.setField(reconnected, "id", 101L);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.of(userToBuddy));
        given(buddyRepository.findByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE))
                .willReturn(java.util.Optional.of(buddyToUser));
        given(userRepository.findByBuddyCode("BBBB2222")).willReturn(java.util.Optional.of(buddy));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE)).willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(2L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRelationshipRepository.saveAndFlush(any(BuddyRelationship.class))).willReturn(newRelationship);
        given(buddyRepository.saveAllAndFlush(any())).willReturn(List.of(reconnected));

        buddyService.removeBuddy(1L, 2L);
        BuddyResponse response = buddyService.connectByBuddyCode(1L, "BBBB2222");

        assertThat(response.buddyUserId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(BuddyStatus.ACTIVE);
    }

    @Test
    void getRandomCandidates_excludesUsersWithRandomMatchingDisabled() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(buddyRepository.findBuddyUserIdsByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(List.of());
        given(buddyRequestRepository.findTargetUserIdsByRequesterIdAndStatus(1L, BuddyRequestStatus.PENDING)).willReturn(List.of());
        given(buddyRequestRepository.findTargetUserIdsByRequesterIdAndStatus(1L, BuddyRequestStatus.REJECTED)).willReturn(List.of());
        given(userRepository.findByRandomMatchingEnabledTrueOrderByIdAsc()).willReturn(List.of());

        List<RandomMatchingCandidateResponse> responses = buddyService.getRandomCandidates(1L);

        assertThat(responses).isEmpty();
    }

    @Test
    void getRandomCandidates_excludesExistingBuddy() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User candidate = new User(2L, "b", WordLevel.N4, "BBBB2222", null, "@b", "bio", true);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(buddyRepository.findBuddyUserIdsByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(List.of(2L));
        given(buddyRequestRepository.findTargetUserIdsByRequesterIdAndStatus(1L, BuddyRequestStatus.PENDING)).willReturn(List.of());
        given(buddyRequestRepository.findTargetUserIdsByRequesterIdAndStatus(1L, BuddyRequestStatus.REJECTED)).willReturn(List.of());
        given(userRepository.findByRandomMatchingEnabledTrueOrderByIdAsc()).willReturn(List.of(candidate));

        List<RandomMatchingCandidateResponse> responses = buddyService.getRandomCandidates(1L);

        assertThat(responses).isEmpty();
    }

    @Test
    void getRandomCandidates_includesDifferentLearningLevelCandidate() {
        User user = new User(1L, "a", WordLevel.N4, "AAAA1111");
        User candidate = new User(2L, "b", WordLevel.N2, "BBBB2222", null, "@b", "bio", true);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(buddyRepository.findBuddyUserIdsByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(List.of());
        given(buddyRequestRepository.findTargetUserIdsByRequesterIdAndStatus(1L, BuddyRequestStatus.PENDING)).willReturn(List.of());
        given(buddyRequestRepository.findTargetUserIdsByRequesterIdAndStatus(1L, BuddyRequestStatus.REJECTED)).willReturn(List.of());
        given(userRepository.findByRandomMatchingEnabledTrueOrderByIdAsc()).willReturn(List.of(candidate));
        given(buddyRepository.countByUserIdAndStatus(2L, BuddyStatus.ACTIVE)).willReturn(0L);

        List<RandomMatchingCandidateResponse> responses = buddyService.getRandomCandidates(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(2L);
        assertThat(responses.get(0).learningLevel()).isEqualTo(WordLevel.N2);
    }
}
