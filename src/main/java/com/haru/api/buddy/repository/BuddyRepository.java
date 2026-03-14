package com.haru.api.buddy.repository;

import com.haru.api.buddy.domain.Buddy;
import com.haru.api.buddy.domain.BuddyStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BuddyRepository extends JpaRepository<Buddy, Long> {

    long countByUserIdAndStatus(Long userId, BuddyStatus status);

    boolean existsByUserIdAndBuddyUserId(Long userId, Long buddyUserId);

    boolean existsByUserIdAndBuddyUserIdAndStatus(Long userId, Long buddyUserId, BuddyStatus status);

    List<Buddy> findByUserIdAndStatusOrderByCreatedAtAsc(Long userId, BuddyStatus status);

    Optional<Buddy> findByUserIdAndBuddyUserIdAndStatus(Long userId, Long buddyUserId, BuddyStatus status);

    @Query("""
            select b.buddyUser.id
            from Buddy b
            where b.user.id = :userId
              and b.status = :status
            """)
    List<Long> findBuddyUserIdsByUserIdAndStatus(Long userId, BuddyStatus status);

    @Query("""
            select count(b)
            from Buddy b
            left join b.buddyRelationship br
            where br is null
            """)
    long countWithMissingRelationship();
}
