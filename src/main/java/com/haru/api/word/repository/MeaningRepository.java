package com.haru.api.word.repository;

import com.haru.api.word.domain.Meaning;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeaningRepository extends JpaRepository<Meaning, Long> {

    List<Meaning> findByWordIdOrderByOrdAsc(Long wordId);

    List<Meaning> findByWordIdNot(Long wordId);
}
