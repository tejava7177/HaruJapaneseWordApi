package com.haru.api.buddy.service;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.dto.BuddyResponse;
import com.haru.api.buddy.repository.BuddyRepository;
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

    private final BuddyRepository buddyRepository;
    private final UserRepository userRepository;

    public List<BuddyResponse> getBuddies(Long userId) {
        ensureUserExists(userId);
        return buddyRepository.findByUserIdAndStatusOrderByCreatedAtAsc(userId, BuddyStatus.ACTIVE)
                .stream()
                .map(BuddyResponse::from)
                .toList();
    }

    @Transactional
    public BuddyResponse addBuddy(Long userId, Long buddyUserId) {
        if (userId.equals(buddyUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add yourself as buddy");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        User buddyUser = userRepository.findById(buddyUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + buddyUserId));

        if (buddyRepository.existsByUserIdAndBuddyUserId(userId, buddyUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buddy already exists");
        }

        long currentBuddyCount = buddyRepository.countByUserIdAndStatus(userId, BuddyStatus.ACTIVE);
        if (currentBuddyCount >= MAX_BUDDY_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user can have up to 3 buddies");
        }

        Buddy saved = buddyRepository.save(Buddy.active(user, buddyUser));
        return BuddyResponse.from(saved);
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }
}
