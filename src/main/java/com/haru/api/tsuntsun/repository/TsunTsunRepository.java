package com.haru.api.tsuntsun.repository;

import com.haru.api.tsuntsun.domain.TsunTsun;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsunTsunRepository extends JpaRepository<TsunTsun, Long> {

    long countBySenderIdAndTargetDate(Long senderId, LocalDate targetDate);

    boolean existsBySenderIdAndReceiverIdAndDailyWordItemIdAndTargetDateAndStatus(
            Long senderId,
            Long receiverId,
            Long dailyWordItemId,
            LocalDate targetDate,
            TsunTsunStatus status
    );

    @EntityGraph(attributePaths = {"sender", "receiver", "word"})
    List<TsunTsun> findByReceiverIdAndTargetDateOrderByCreatedAtDesc(Long receiverId, LocalDate targetDate);

    @EntityGraph(attributePaths = {"word"})
    Optional<TsunTsun> findWithWordById(Long id);
}
