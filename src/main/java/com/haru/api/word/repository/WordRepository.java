package com.haru.api.word.repository;

import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findTop20ByOrderByIdAsc();

    List<Word> findByLevelOrderByIdAsc(WordLevel level);

    List<Word> findByLevelAndIdNotOrderByIdAsc(WordLevel level, Long id);

    @EntityGraph(attributePaths = "meanings")
    Optional<Word> findWithMeaningsById(Long id);
}
