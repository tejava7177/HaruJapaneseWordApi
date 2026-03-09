package com.haru.api.dailyword.repository;

import com.haru.api.dailyword.domain.DailyWordItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyWordItemRepository extends JpaRepository<DailyWordItem, Long> {
}
