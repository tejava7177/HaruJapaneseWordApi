package com.haru.api.reviewword.repository;

import com.haru.api.reviewword.domain.ReviewWord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewWordRepository extends JpaRepository<ReviewWord, Long> {

    List<ReviewWord> findByUserIdOrderByWordIdAsc(Long userId);

    Optional<ReviewWord> findByUserIdAndWordId(Long userId, Long wordId);

    boolean existsByUserIdAndWordId(Long userId, Long wordId);
}
