package com.haru.api.buddy.repository;

import com.haru.api.buddy.domain.BuddyRequest;
import com.haru.api.buddy.domain.BuddyRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BuddyRequestRepository extends JpaRepository<BuddyRequest, Long> {

    long countByRequesterIdAndStatus(Long requesterId, BuddyRequestStatus status);

    boolean existsByRequesterIdAndTargetUserIdAndStatus(Long requesterId, Long targetUserId, BuddyRequestStatus status);

    @EntityGraph(attributePaths = {"requester", "targetUser"})
    List<BuddyRequest> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

    @EntityGraph(attributePaths = {"requester", "targetUser"})
    List<BuddyRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @EntityGraph(attributePaths = {"requester", "targetUser"})
    java.util.Optional<BuddyRequest> findWithUsersById(Long id);

    @Query("""
            select br.targetUser.id
            from BuddyRequest br
            where br.requester.id = :requesterId
              and br.status = :status
            """)
    List<Long> findTargetUserIdsByRequesterIdAndStatus(Long requesterId, BuddyRequestStatus status);
}
