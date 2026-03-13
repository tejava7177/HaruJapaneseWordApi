package com.haru.api.buddy.service;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.dto.BuddyResponse;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuddyService {

    private static final int MAX_BUDDY_COUNT = 3;

    private final BuddyRelationshipRepository buddyRelationshipRepository;
    private final BuddyRepository buddyRepository;
    private final TsunTsunRepository tsunTsunRepository;
    private final UserRepository userRepository;

    public List<BuddyResponse> getBuddies(Long userId) {
        ensureUserExists(userId);
        return buddyRepository.findByUserIdAndStatusOrderByCreatedAtAsc(userId, BuddyStatus.ACTIVE)
                .stream()
                .map(this::toBuddyResponse)
                .toList();
    }

    @Transactional
    public BuddyResponse connectByBuddyCode(Long userId, String buddyCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        User buddyUser = userRepository.findByBuddyCode(buddyCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buddy not found with code: " + buddyCode));

        return connectUsers(user, buddyUser);
    }

    @Transactional
    public BuddyResponse addBuddy(Long userId, Long buddyUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        User buddyUser = userRepository.findById(buddyUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + buddyUserId));

        return connectUsers(user, buddyUser);
    }

    private BuddyResponse connectUsers(User user, User buddyUser) {
        if (user.getId().equals(buddyUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신의 buddyCode로는 연결할 수 없습니다.");
        }

        if (buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(user.getId(), buddyUser.getId(), BuddyStatus.ACTIVE)
                || buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(buddyUser.getId(), user.getId(), BuddyStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 연결된 버디입니다.");
        }

        validateBuddyLimit(user.getId());
        validateBuddyLimit(buddyUser.getId());

        BuddyRelationship buddyRelationship = buddyRelationshipRepository.saveAndFlush(BuddyRelationship.create());
        Long relationshipId = buddyRelationship.getId();
        if (relationshipId == null) {
            throw new IllegalStateException("BuddyRelationship id was not generated");
        }
        org.slf4j.LoggerFactory.getLogger(BuddyService.class).info(
                "[buddy/relationship] created relationship first: relationshipId={}, userId={}, buddyUserId={}",
                relationshipId, user.getId(), buddyUser.getId()
        );
        Buddy userToBuddy = Buddy.active(user, buddyUser, buddyRelationship);
        Buddy buddyToUser = Buddy.active(buddyUser, user, buddyRelationship);
        buddyRepository.saveAll(List.of(userToBuddy, buddyToUser));
        org.slf4j.LoggerFactory.getLogger(BuddyService.class).info(
                "[buddy/relationship] linked relationship to buddy rows: relationshipId={}, userId={}, buddyUserId={}",
                relationshipId, user.getId(), buddyUser.getId()
        );

        return BuddyResponse.from(userToBuddy, 0L);
    }

    private BuddyResponse toBuddyResponse(Buddy buddy) {
        Long buddyRelationshipId = buddy.getBuddyRelationship().getId();
        long answeredToBuddyCount = tsunTsunRepository.countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
                buddyRelationshipId,
                buddy.getUser().getId(),
                buddy.getBuddyUser().getId(),
                TsunTsunStatus.ANSWERED
        );
        long answeredFromBuddyCount = tsunTsunRepository.countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
                buddyRelationshipId,
                buddy.getBuddyUser().getId(),
                buddy.getUser().getId(),
                TsunTsunStatus.ANSWERED
        );
        long tikiTakaCount = Math.min(answeredToBuddyCount, answeredFromBuddyCount);
        return BuddyResponse.from(buddy, tikiTakaCount);
    }

    private void validateBuddyLimit(Long userId) {
        long currentBuddyCount = buddyRepository.countByUserIdAndStatus(userId, BuddyStatus.ACTIVE);
        if (currentBuddyCount >= MAX_BUDDY_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "한 사용자는 최대 3명의 버디만 연결할 수 있습니다.");
        }
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }
}
