package com.haru.api.buddy.service;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.buddy.dto.BuddyRequestActionResponse;
import com.haru.api.buddy.dto.IncomingBuddyRequestResponse;
import com.haru.api.buddy.dto.OutgoingBuddyRequestResponse;
import com.haru.api.buddy.repository.BuddyRequestRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuddyRequestService {

    private static final int MAX_OUTGOING_PENDING_REQUEST_COUNT = 3;
    private static final int MAX_BUDDY_COUNT = 3;

    private final BuddyRequestRepository buddyRequestRepository;
    private final BuddyRepository buddyRepository;
    private final UserRepository userRepository;
    private final BuddyService buddyService;

    @Transactional
    public BuddyRequestActionResponse createRequest(Long requesterId, Long targetUserId) {
        log.info("[BuddyRequest] create start requester={} target={}", requesterId, targetUserId);

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + requesterId));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + targetUserId));

        validateRequestCreation(requester, targetUser);

        BuddyRequest buddyRequest = buddyRequestRepository.save(BuddyRequest.pending(requester, targetUser));
        log.info(
                "[BuddyRequest] create success requester={} target={} requestId={}",
                requesterId,
                targetUserId,
                buddyRequest.getId()
        );
        return BuddyRequestActionResponse.from(buddyRequest);
    }

    public java.util.List<IncomingBuddyRequestResponse> getIncomingRequests(Long userId) {
        ensureUserExists(userId);
        return buddyRequestRepository.findByTargetUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(IncomingBuddyRequestResponse::from)
                .toList();
    }

    public java.util.List<OutgoingBuddyRequestResponse> getOutgoingRequests(Long userId) {
        ensureUserExists(userId);
        return buddyRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OutgoingBuddyRequestResponse::from)
                .toList();
    }

    @Transactional
    public BuddyRequestActionResponse acceptRequest(Long requestId) {
        BuddyRequest buddyRequest = buddyRequestRepository.findWithUsersById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buddy request not found: " + requestId));

        if (buddyRequest.getStatus() != BuddyRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 처리된 버디 신청입니다.");
        }

        buddyService.connectUsers(buddyRequest.getRequester(), buddyRequest.getTargetUser());
        buddyRequest.accept();
        return BuddyRequestActionResponse.from(buddyRequest);
    }

    @Transactional
    public BuddyRequestActionResponse rejectRequest(Long requestId) {
        BuddyRequest buddyRequest = buddyRequestRepository.findWithUsersById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buddy request not found: " + requestId));

        if (buddyRequest.getStatus() != BuddyRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 처리된 버디 신청입니다.");
        }

        buddyRequest.reject();
        return BuddyRequestActionResponse.from(buddyRequest);
    }

    private void validateRequestCreation(User requester, User targetUser) {
        boolean selfRequest = requester.getId().equals(targetUser.getId());
        log.info("[BuddyRequest] validation selfRequest={}", selfRequest);
        if (selfRequest) {
            rejectCreateRequest(requester.getId(), targetUser.getId(), "self request", "자기 자신에게 신청할 수 없습니다.");
        }

        // TODO: 2차에서는 같은 레벨 우선, 인접 레벨 허용 정책으로 확장할 수 있다.

        boolean targetRandomMatchingEnabled = targetUser.isRandomMatchingEnabled();
        log.info("[BuddyRequest] validation targetRandomMatchingEnabled={}", targetRandomMatchingEnabled);
        if (!targetRandomMatchingEnabled) {
            rejectCreateRequest(
                    requester.getId(),
                    targetUser.getId(),
                    "target random matching disabled",
                    "상대가 랜덤 매칭 노출을 꺼두었습니다."
            );
        }

        boolean alreadyBuddy = buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(
                requester.getId(),
                targetUser.getId(),
                BuddyStatus.ACTIVE
        ) || buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(
                targetUser.getId(),
                requester.getId(),
                BuddyStatus.ACTIVE
        );
        log.info("[BuddyRequest] validation alreadyBuddy={}", alreadyBuddy);
        if (alreadyBuddy) {
            rejectCreateRequest(requester.getId(), targetUser.getId(), "already buddy", "이미 버디인 사용자입니다.");
        }

        boolean duplicatePending = buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(
                requester.getId(), targetUser.getId(), BuddyRequestStatus.PENDING
        );
        log.info("[BuddyRequest] validation duplicatePending={}", duplicatePending);
        if (duplicatePending) {
            rejectCreateRequest(
                    requester.getId(),
                    targetUser.getId(),
                    "duplicate pending",
                    "이미 대기 중인 버디 신청이 있습니다."
            );
        }

        boolean rejectedHistory = buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(
                requester.getId(), targetUser.getId(), BuddyRequestStatus.REJECTED
        );
        log.info("[BuddyRequest] validation rejectedHistory={}", rejectedHistory);
        if (rejectedHistory) {
            rejectCreateRequest(
                    requester.getId(),
                    targetUser.getId(),
                    "rejected history exists",
                    "이미 거절된 버디 신청 이력이 있습니다."
            );
        }

        long requesterBuddyCount = buddyRepository.countByUserIdAndStatus(requester.getId(), BuddyStatus.ACTIVE);
        log.info("[BuddyRequest] validation requesterBuddyCount={}", requesterBuddyCount);
        if (requesterBuddyCount >= MAX_BUDDY_COUNT) {
            rejectCreateRequest(
                    requester.getId(),
                    targetUser.getId(),
                    "requester buddy limit reached",
                    "현재 버디 수가 가득 찼습니다. 기존 버디를 정리한 뒤 다시 시도해주세요."
            );
        }

        long targetBuddyCount = buddyRepository.countByUserIdAndStatus(targetUser.getId(), BuddyStatus.ACTIVE);
        log.info("[BuddyRequest] validation targetBuddyCount={}", targetBuddyCount);
        if (targetBuddyCount >= MAX_BUDDY_COUNT) {
            rejectCreateRequest(
                    requester.getId(),
                    targetUser.getId(),
                    "target buddy limit reached",
                    "상대의 버디 수가 가득 찼습니다."
            );
        }

        long requesterPendingCount = buddyRequestRepository.countByRequesterIdAndStatus(
                requester.getId(),
                BuddyRequestStatus.PENDING
        );
        log.info("[BuddyRequest] validation requesterPendingCount={}", requesterPendingCount);
        if (requesterPendingCount >= MAX_OUTGOING_PENDING_REQUEST_COUNT) {
            rejectCreateRequest(
                    requester.getId(),
                    targetUser.getId(),
                    "requester pending limit reached",
                    "현재 대기 중인 버디 신청이 %d개예요. 응답을 기다려주세요.".formatted(requesterPendingCount)
            );
        }
    }

    private void rejectCreateRequest(Long requesterId, Long targetUserId, String reason, String message) {
        log.warn("[BuddyRequest] create rejected requester={} target={} reason={}", requesterId, targetUserId, reason);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }
}
