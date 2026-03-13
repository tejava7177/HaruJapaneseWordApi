package com.haru.api.buddy.init;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class BuddyDataInitializer implements CommandLineRunner {

    private static final List<long[]> TEST_BUDDY_PAIRS = List.of(
            new long[]{1L, 2L},
            new long[]{1L, 3L},
            new long[]{1L, 4L},
            new long[]{2L, 3L},
            new long[]{2L, 4L},
            new long[]{3L, 4L}
    );

    private final BuddyRelationshipRepository buddyRelationshipRepository;
    private final BuddyRepository buddyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (long[] pair : TEST_BUDDY_PAIRS) {
            ensureBidirectionalBuddy(pair[0], pair[1]);
        }

        logActiveBuddyState();
    }

    private void ensureBidirectionalBuddy(Long userId, Long buddyUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Missing test user: " + userId));
        User buddyUser = userRepository.findById(buddyUserId)
                .orElseThrow(() -> new IllegalStateException("Missing test user: " + buddyUserId));

        boolean exists = buddyRepository.existsByUserIdAndBuddyUserIdAndStatus(
                user.getId(),
                buddyUser.getId(),
                BuddyStatus.ACTIVE
        );

        if (exists) {
            log.info("[init] buddy relation already exists: userId={}, buddyId={}", user.getId(), buddyUser.getId());
            return;
        }

        BuddyRelationship buddyRelationship = buddyRelationshipRepository.saveAndFlush(BuddyRelationship.create());
        Long relationshipId = buddyRelationship.getId();
        log.info("[init] buddy relationship created first: relationshipId={}, userId={}, buddyId={}",
                relationshipId, user.getId(), buddyUser.getId());
        buddyRepository.saveAll(List.of(
                Buddy.active(user, buddyUser, buddyRelationship),
                Buddy.active(buddyUser, user, buddyRelationship)
        ));
        log.info("[init] buddy relation linked bidirectionally: userId={}, buddyId={}, relationshipId={}",
                user.getId(), buddyUser.getId(), relationshipId);
    }

    private void logActiveBuddyState() {
        List<Buddy> activeBuddies = buddyRepository.findAll().stream()
                .filter(buddy -> buddy.getStatus() == BuddyStatus.ACTIVE)
                .sorted(Comparator
                        .comparing((Buddy buddy) -> buddy.getUser().getId())
                        .thenComparing(buddy -> buddy.getBuddyUser().getId()))
                .toList();

        if (activeBuddies.isEmpty()) {
            log.warn("[init] active buddy table is empty");
            return;
        }

        activeBuddies.forEach(buddy -> log.info(
                "[init] active buddy row: userId={}, buddyId={}, status={}",
                buddy.getUser().getId(),
                buddy.getBuddyUser().getId(),
                buddy.getStatus()
        ));
    }
}
