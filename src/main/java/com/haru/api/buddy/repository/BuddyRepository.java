package com.haru.api.buddy.repository;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuddyRepository extends JpaRepository<Buddy, Long> {

    long countByUserIdAndStatus(Long userId, BuddyStatus status);

    boolean existsByUserIdAndBuddyUserId(Long userId, Long buddyUserId);

    List<Buddy> findByUserIdAndStatusOrderByCreatedAtAsc(Long userId, BuddyStatus status);
}
