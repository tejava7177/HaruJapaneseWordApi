package com.haru.api.notebook.repository;

import com.haru.api.notebook.domain.NotebookItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotebookItemRepository extends JpaRepository<NotebookItem, Long> {

    Optional<NotebookItem> findByIdAndNotebookId(Long id, Long notebookId);

    boolean existsByNotebookIdAndWordId(Long notebookId, Long wordId);

    boolean existsByNotebookIdAndWordIdAndIdNot(Long notebookId, Long wordId, Long itemId);
}
