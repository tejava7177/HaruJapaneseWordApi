package com.haru.api.buddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.dto.BuddyRequestActionResponse;
import com.haru.api.buddy.dto.IncomingBuddyRequestResponse;
import com.haru.api.buddy.repository.BuddyRequestRepository;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.push.PushNotificationService;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.user.service.ActivityTrackingService;
import com.haru.api.word.domain.WordLevel;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BuddyRequestServiceTest {

    @Mock
    private BuddyRequestRepository buddyRequestRepository;

    @Mock
    private BuddyRepository buddyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BuddyService buddyService;

    @Mock
    private BuddyRelationshipRepository buddyRelationshipRepository;

    @Mock
    private TsunTsunRepository tsunTsunRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private ActivityTrackingService activityTrackingService;

    private BuddyRequestService buddyRequestService;

    @BeforeEach
    void setUp() {
        buddyRequestService = new BuddyRequestService(
                buddyRequestRepository,
                buddyRepository,
                userRepository,
                buddyService,
                pushNotificationService
        );
    }

    @Test
    void createRequest_failsWhenOutgoingPendingLimitExceeded() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(requester));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(target));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE))
                .willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE))
                .willReturn(false);
        given(buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(1L, 2L, BuddyRequestStatus.PENDING))
                .willReturn(false);
        given(buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(1L, 2L, BuddyRequestStatus.REJECTED))
                .willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(1L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(2L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRequestRepository.countByRequesterIdAndStatus(1L, BuddyRequestStatus.PENDING)).willReturn(3L);

        assertThatThrownBy(() -> buddyRequestService.createRequest(1L, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("대기 중인 버디 신청이 3개");
    }

    @Test
    void createRequest_createsBuddyRequestWhenStateIsClean() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);
        BuddyRequest savedRequest = BuddyRequest.pending(requester, target);
        ReflectionTestUtils.setField(savedRequest, "id", 10L);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(requester));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(target));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE))
                .willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE))
                .willReturn(false);
        given(buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(1L, 2L, BuddyRequestStatus.PENDING))
                .willReturn(false);
        given(buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(1L, 2L, BuddyRequestStatus.REJECTED))
                .willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(1L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(2L, com.haru.api.buddy.domain.BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRequestRepository.countByRequesterIdAndStatus(1L, BuddyRequestStatus.PENDING)).willReturn(0L);
        given(buddyRequestRepository.save(any(BuddyRequest.class))).willReturn(savedRequest);

        BuddyRequestActionResponse response = buddyRequestService.createRequest(1L, 2L);

        assertThat(response.requestId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(BuddyRequestStatus.PENDING);
        verify(buddyRequestRepository).save(any(BuddyRequest.class));
        verify(pushNotificationService).notifyBuddyRequestReceived(2L, 10L, 1L);
    }

    @Test
    void createRequest_succeedsWhenRequesterIsN3AndTargetIsN2() {
        User requester = new User(1L, "requester", WordLevel.N3, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N2, "TAR00001", null, "@target", "bio", true);

        BuddyRequestActionResponse response = createCleanBuddyRequest(requester, target, 11L);

        assertThat(response.requestId()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo(BuddyRequestStatus.PENDING);
        verify(buddyRequestRepository).save(any(BuddyRequest.class));
    }

    @Test
    void createRequest_succeedsWhenRequesterIsN3AndTargetIsN1() {
        User requester = new User(1L, "requester", WordLevel.N3, "REQ00001", null, "@req", "bio", true);
        User target = new User(4L, "target", WordLevel.N1, "TAR00004", null, "@target", "bio", true);

        BuddyRequestActionResponse response = createCleanBuddyRequest(requester, target, 12L);

        assertThat(response.requestId()).isEqualTo(12L);
        assertThat(response.status()).isEqualTo(BuddyRequestStatus.PENDING);
        verify(buddyRequestRepository).save(any(BuddyRequest.class));
    }

    @Test
    void getIncomingRequests_returnsRequesterProfiles() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);
        BuddyRequest buddyRequest = BuddyRequest.pending(requester, target);
        ReflectionTestUtils.setField(buddyRequest, "id", 10L);

        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRequestRepository.findByTargetUserIdAndStatusOrderByCreatedAtDesc(2L, BuddyRequestStatus.PENDING))
                .willReturn(List.of(buddyRequest));

        List<IncomingBuddyRequestResponse> responses = buddyRequestService.getIncomingRequests(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).requestId()).isEqualTo(10L);
        assertThat(responses.get(0).requesterId()).isEqualTo(1L);
        assertThat(responses.get(0).status()).isEqualTo(BuddyRequestStatus.PENDING);
    }

    @Test
    void getIncomingRequests_excludesAcceptedRequests() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRequestRepository.findByTargetUserIdAndStatusOrderByCreatedAtDesc(2L, BuddyRequestStatus.PENDING))
                .willReturn(List.of());

        List<IncomingBuddyRequestResponse> responses = buddyRequestService.getIncomingRequests(2L);

        assertThat(responses).isEmpty();
    }

    @Test
    void getIncomingRequests_excludesRejectedRequests() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRequestRepository.findByTargetUserIdAndStatusOrderByCreatedAtDesc(2L, BuddyRequestStatus.PENDING))
                .willReturn(List.of());

        List<IncomingBuddyRequestResponse> responses = buddyRequestService.getIncomingRequests(2L);

        assertThat(responses).isEmpty();
    }

    @Test
    void getOutgoingRequests_returnsPendingRequestsOnly() {
        User requester = new User(2L, "requester", WordLevel.N4, "REQ00002", null, "@req", "bio", true);
        User target = new User(3L, "target", WordLevel.N4, "TAR00003", null, "@target", "bio", true);
        BuddyRequest buddyRequest = BuddyRequest.pending(requester, target);
        ReflectionTestUtils.setField(buddyRequest, "id", 20L);

        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRequestRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(2L, BuddyRequestStatus.PENDING))
                .willReturn(List.of(buddyRequest));

        List<com.haru.api.buddy.dto.OutgoingBuddyRequestResponse> responses = buddyRequestService.getOutgoingRequests(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).requestId()).isEqualTo(20L);
        assertThat(responses.get(0).targetUserId()).isEqualTo(3L);
        assertThat(responses.get(0).status()).isEqualTo(BuddyRequestStatus.PENDING);
    }

    @Test
    void getOutgoingRequests_excludesAcceptedRequests() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(buddyRequestRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(2L, BuddyRequestStatus.PENDING))
                .willReturn(List.of());

        List<com.haru.api.buddy.dto.OutgoingBuddyRequestResponse> responses = buddyRequestService.getOutgoingRequests(2L);

        assertThat(responses).isEmpty();
    }

    @Test
    void acceptRequest_createsBuddyConnectionAndChangesStatus() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);
        BuddyRequest buddyRequest = pendingRequest(10L, requester, target);
        BuddyRelationship relationship = BuddyRelationship.create();
        ReflectionTestUtils.setField(relationship, "id", 20L);
        BuddyService realBuddyService = new BuddyService(
                buddyRelationshipRepository,
                buddyRepository,
                buddyRequestRepository,
                tsunTsunRepository,
                userRepository,
                Clock.fixed(Instant.parse("2026-03-27T15:00:00Z"), ZoneId.of("Asia/Seoul"))
        );
        BuddyRequestService acceptService = new BuddyRequestService(
                buddyRequestRepository,
                buddyRepository,
                userRepository,
                realBuddyService,
                pushNotificationService
        );

        given(buddyRequestRepository.findWithUsersById(10L)).willReturn(java.util.Optional.of(buddyRequest));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE))
                .willReturn(false, true);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(2L, 1L, BuddyStatus.ACTIVE))
                .willReturn(false, true);
        given(buddyRepository.countByUserIdAndStatus(1L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(2L, BuddyStatus.ACTIVE)).willReturn(0L);
        given(buddyRelationshipRepository.saveAndFlush(any(BuddyRelationship.class))).willReturn(relationship);
        given(buddyRepository.saveAllAndFlush(any())).willAnswer(invocation -> invocation.getArgument(0));

        BuddyRequestActionResponse response = acceptService.acceptRequest(10L);

        verify(buddyRepository).saveAllAndFlush(any());
        assertThat(response.requestId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(BuddyRequestStatus.ACCEPTED);
        assertThat(buddyRequest.getStatus()).isEqualTo(BuddyRequestStatus.ACCEPTED);
        assertThat(buddyRequest.getRespondedAt()).isNotNull();
        verify(pushNotificationService).notifyBuddyAccepted(1L, 10L, 2L);
    }

    @Test
    void acceptRequest_rollsBackStatusWhenBuddyCreationFails() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);
        BuddyRequest buddyRequest = pendingRequest(10L, requester, target);

        given(buddyRequestRepository.findWithUsersById(10L)).willReturn(java.util.Optional.of(buddyRequest));
        org.mockito.Mockito.doThrow(new IllegalStateException("buddy insert failed"))
                .when(buddyService)
                .connectUsers(requester, target);

        assertThatThrownBy(() -> buddyRequestService.acceptRequest(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("buddy insert failed");

        assertThat(buddyRequest.getStatus()).isEqualTo(BuddyRequestStatus.PENDING);
        assertThat(buddyRequest.getRespondedAt()).isNull();
    }

    @Test
    void acceptRequest_rejectsAcceptedStateWhenBuddyRowsAreMissingAfterConnect() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);
        BuddyRequest buddyRequest = pendingRequest(10L, requester, target);

        given(buddyRequestRepository.findWithUsersById(10L)).willReturn(java.util.Optional.of(buddyRequest));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(1L, 2L, BuddyStatus.ACTIVE)).willReturn(false);

        assertThatThrownBy(() -> buddyRequestService.acceptRequest(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Buddy rows were not created");

        assertThat(buddyRequest.getStatus()).isEqualTo(BuddyRequestStatus.PENDING);
        assertThat(buddyRequest.getRespondedAt()).isNull();
    }

    @Test
    void rejectRequest_changesStatus() {
        User requester = new User(1L, "requester", WordLevel.N4, "REQ00001", null, "@req", "bio", true);
        User target = new User(2L, "target", WordLevel.N4, "TAR00001", null, "@target", "bio", true);
        BuddyRequest buddyRequest = BuddyRequest.pending(requester, target);
        ReflectionTestUtils.setField(buddyRequest, "id", 10L);

        given(buddyRequestRepository.findWithUsersById(10L)).willReturn(java.util.Optional.of(buddyRequest));

        BuddyRequestActionResponse response = buddyRequestService.rejectRequest(10L);

        assertThat(response.requestId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(BuddyRequestStatus.REJECTED);
    }

    private BuddyRequestActionResponse createCleanBuddyRequest(User requester, User target, Long requestId) {
        BuddyRequest savedRequest = BuddyRequest.pending(requester, target);
        ReflectionTestUtils.setField(savedRequest, "id", requestId);

        given(userRepository.findById(requester.getId())).willReturn(java.util.Optional.of(requester));
        given(userRepository.findById(target.getId())).willReturn(java.util.Optional.of(target));
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(
                requester.getId(),
                target.getId(),
                com.haru.api.buddy.domain.BuddyStatus.ACTIVE
        )).willReturn(false);
        given(buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(
                target.getId(),
                requester.getId(),
                com.haru.api.buddy.domain.BuddyStatus.ACTIVE
        )).willReturn(false);
        given(buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(
                requester.getId(),
                target.getId(),
                BuddyRequestStatus.PENDING
        )).willReturn(false);
        given(buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(
                requester.getId(),
                target.getId(),
                BuddyRequestStatus.REJECTED
        )).willReturn(false);
        given(buddyRepository.countByUserIdAndStatus(requester.getId(), com.haru.api.buddy.domain.BuddyStatus.ACTIVE))
                .willReturn(0L);
        given(buddyRepository.countByUserIdAndStatus(target.getId(), com.haru.api.buddy.domain.BuddyStatus.ACTIVE))
                .willReturn(0L);
        given(buddyRequestRepository.countByRequesterIdAndStatus(requester.getId(), BuddyRequestStatus.PENDING))
                .willReturn(0L);
        given(buddyRequestRepository.save(any(BuddyRequest.class))).willReturn(savedRequest);

        return buddyRequestService.createRequest(requester.getId(), target.getId());
    }

    private BuddyRequest pendingRequest(Long requestId, User requester, User target) {
        BuddyRequest buddyRequest = BuddyRequest.pending(requester, target);
        ReflectionTestUtils.setField(buddyRequest, "id", requestId);
        return buddyRequest;
    }
}
