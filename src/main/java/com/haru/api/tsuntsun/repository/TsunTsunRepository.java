package com.haru.api.tsuntsun.repository;

import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TsunTsunRepository extends JpaRepository<TsunTsun, Long> {

    long countBySenderIdAndReceiverIdAndTargetDate(Long senderId, Long receiverId, LocalDate targetDate);

    long countBySenderIdAndReceiverIdAndTargetDateAndStatus(
            Long senderId,
            Long receiverId,
            LocalDate targetDate,
            TsunTsunStatus status
    );

    long countByReceiverIdAndStatus(Long receiverId, TsunTsunStatus status);

    long countByBuddyRelationshipIdAndSenderIdAndReceiverIdAndStatus(
            Long buddyRelationshipId,
            Long senderId,
            Long receiverId,
            TsunTsunStatus status
    );

    boolean existsByBuddyRelationshipIdAndSenderIdAndReceiverIdAndTargetDateAndStatus(
            Long buddyRelationshipId,
            Long senderId,
            Long receiverId,
            LocalDate targetDate,
            TsunTsunStatus status
    );

    boolean existsBySenderIdAndReceiverIdAndTargetDateAndStatus(
            Long senderId,
            Long receiverId,
            LocalDate targetDate,
            TsunTsunStatus status
    );

    boolean existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDate(
            Long senderId,
            Long receiverId,
            Long dailyWordItemId,
            LocalDate targetDate
    );

    @EntityGraph(attributePaths = {"sender", "receiver", "word", "dailyWordItem"})
    @Query("""
            select t
            from TsunTsun t
            where t.targetDate = :targetDate
              and ((t.sender.id = :userId and t.receiver.id = :buddyId)
                or (t.sender.id = :buddyId and t.receiver.id = :userId))
            order by t.createdAt desc
            """)
    List<TsunTsun> findPairByTargetDate(Long userId, Long buddyId, LocalDate targetDate);

    @EntityGraph(attributePaths = {"word"})
    Optional<TsunTsun> findWithWordById(Long id);

    Optional<TsunTsun> findTopByBuddyRelationshipIdAndSenderIdAndReceiverIdOrderByCreatedAtDesc(
            Long buddyRelationshipId,
            Long senderId,
            Long receiverId
    );

    Optional<TsunTsun> findTopByBuddyRelationshipIdOrderByCreatedAtDesc(Long buddyRelationshipId);

    @EntityGraph(attributePaths = {"sender", "word"})
    List<TsunTsun> findByReceiverIdAndTargetDateAndStatusOrderByCreatedAtDesc(
            Long receiverId,
            LocalDate targetDate,
            TsunTsunStatus status
    );

    List<TsunTsun> findByDailyWordItemIdInAndTargetDate(List<Long> dailyWordItemIds, LocalDate targetDate);

    void deleteByDailyWordItemIdInAndTargetDate(List<Long> dailyWordItemIds, LocalDate targetDate);

    @Query("""
            select count(t)
            from TsunTsun t
            left join t.buddyRelationship br
            where br is null
            """)
    long countWithMissingRelationship();
}
