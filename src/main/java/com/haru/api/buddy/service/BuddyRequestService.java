package com.haru.api.buddy.service;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.buddy.dto.BuddyRequestActionResponse;
import com.haru.api.buddy.dto.IncomingBuddyRequestResponse;
import com.haru.api.buddy.dto.OutgoingBuddyRequestResponse;
import com.haru.api.buddy.repository.BuddyRequestRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuddyRequestService {

    private static final int MAX_OUTGOING_PENDING_REQUEST_COUNT = 3;

    private final BuddyRequestRepository buddyRequestRepository;
    private final BuddyRepository buddyRepository;
    private final UserRepository userRepository;
    private final BuddyService buddyService;

    @Transactional
    public BuddyRequestActionResponse createRequest(Long requesterId, Long targetUserId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + requesterId));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + targetUserId));

        validateRequestCreation(requester, targetUser);

        BuddyRequest buddyRequest = buddyRequestRepository.save(BuddyRequest.pending(requester, targetUser));
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
        if (requester.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게는 버디 신청을 보낼 수 없습니다.");
        }

        if (requester.getLearningLevel() != targetUser.getLearningLevel()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 1차 랜덤 매칭은 같은 학습 레벨 사용자에게만 신청할 수 있습니다.");
        }

        if (!targetUser.isRandomMatchingEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상대방이 랜덤 매칭 노출을 비활성화했습니다.");
        }

        if (buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(
                requester.getId(), targetUser.getId(), com.haru.api.buddy.domain.BuddyStatus.ACTIVE
        )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 연결된 버디입니다.");
        }

        if (buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(
                requester.getId(), targetUser.getId(), BuddyRequestStatus.PENDING
        )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 대기 중인 버디 신청입니다.");
        }

        if (buddyRequestRepository.existsByRequesterIdAndTargetUserIdAndStatus(
                requester.getId(), targetUser.getId(), BuddyRequestStatus.REJECTED
        )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 거절된 랜덤 매칭 신청입니다.");
        }

        if (buddyRequestRepository.countByRequesterIdAndStatus(requester.getId(), BuddyRequestStatus.PENDING)
                >= MAX_OUTGOING_PENDING_REQUEST_COUNT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "현재 대기 중인 버디 신청이 3개예요. 응답을 기다려주세요."
            );
        }

        buddyService.validateBuddyLimitAvailable(requester.getId());
        buddyService.validateBuddyLimitAvailable(targetUser.getId());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }
}
