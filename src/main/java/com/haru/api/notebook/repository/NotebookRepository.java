package com.haru.api.notebook.repository;

import com.haru.api.notebook.domain.Notebook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotebookRepository extends JpaRepository<Notebook, Long> {

    @EntityGraph(attributePaths = "items")
    List<Notebook> findByUserIdOrderByCreatedAtAscIdAsc(Long userId);

    @EntityGraph(attributePaths = "items")
    Optional<Notebook> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
