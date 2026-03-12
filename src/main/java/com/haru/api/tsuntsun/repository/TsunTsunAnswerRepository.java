package com.haru.api.tsuntsun.repository;

import com.haru.api.tsuntsun.domain.TsunTsunAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsunTsunAnswerRepository extends JpaRepository<TsunTsunAnswer, Long> {

    void deleteByTsuntsunIdIn(List<Long> tsuntsunIds);
}
