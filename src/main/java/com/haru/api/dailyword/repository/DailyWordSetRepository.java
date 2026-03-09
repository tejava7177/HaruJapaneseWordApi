package com.haru.api.dailyword.repository;

import com.haru.api.dailyword.domain.DailyWordSet;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyWordSetRepository extends JpaRepository<DailyWordSet, Long> {

    Optional<DailyWordSet> findByUserIdAndTargetDate(Long userId, LocalDate targetDate);

    @EntityGraph(attributePaths = {"items", "items.word"})
    Optional<DailyWordSet> findWithItemsByUserIdAndTargetDate(Long userId, LocalDate targetDate);
}
