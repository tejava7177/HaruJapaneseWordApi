package com.haru.api.word.repository;

import com.haru.api.word.domain.Meaning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeaningRepository extends JpaRepository<Meaning, Long> {
}
