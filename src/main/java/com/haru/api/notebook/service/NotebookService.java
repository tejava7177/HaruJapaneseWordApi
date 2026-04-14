package com.haru.api.notebook.service;

import com.haru.api.notebook.domain.Notebook;
import com.haru.api.notebook.domain.NotebookItem;
import com.haru.api.notebook.domain.NotebookItemType;
import com.haru.api.notebook.dto.NotebookCreateRequest;
import com.haru.api.notebook.dto.NotebookDeleteResponse;
import com.haru.api.notebook.dto.NotebookItemCreateRequest;
import com.haru.api.notebook.dto.NotebookItemDeleteResponse;
import com.haru.api.notebook.dto.NotebookItemResponse;
import com.haru.api.notebook.dto.NotebookItemUpdateRequest;
import com.haru.api.notebook.dto.NotebookListResponse;
import com.haru.api.notebook.dto.NotebookMigrationItemRequest;
import com.haru.api.notebook.dto.NotebookMigrationNotebookRequest;
import com.haru.api.notebook.dto.NotebookMigrationRequest;
import com.haru.api.notebook.dto.NotebookMigrationResponse;
import com.haru.api.notebook.dto.NotebookResponse;
import com.haru.api.notebook.dto.NotebookUpdateRequest;
import com.haru.api.notebook.repository.NotebookItemRepository;
import com.haru.api.notebook.repository.NotebookRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.Word;
import com.haru.api.word.repository.WordRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookService {

    private final NotebookRepository notebookRepository;
    private final NotebookItemRepository notebookItemRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    public NotebookListResponse getNotebooks(Long userId) {
        ensureUserExists(userId);
        List<NotebookResponse> notebooks = notebookRepository.findByUserIdOrderByCreatedAtAscIdAsc(userId).stream()
                .map(NotebookResponse::from)
                .toList();
        return new NotebookListResponse(userId, notebooks);
    }

    public NotebookResponse getNotebook(Long userId, Long notebookId) {
        return NotebookResponse.from(findNotebookOrThrow(userId, notebookId));
    }

    @Transactional
    public NotebookResponse createNotebook(Long userId, NotebookCreateRequest request) {
        User user = findUserOrThrow(userId);
        Notebook notebook = Notebook.create(
                user,
                normalizeRequired(request.title(), "title"),
                normalizeOptional(request.description())
        );
        return NotebookResponse.from(notebookRepository.save(notebook));
    }

    @Transactional
    public NotebookResponse updateNotebook(Long userId, Long notebookId, NotebookUpdateRequest request) {
        Notebook notebook = findNotebookOrThrow(userId, notebookId);

        String updatedTitle = notebook.getTitle();
        if (request.title() != null) {
            updatedTitle = normalizeRequired(request.title(), "title");
        }

        String updatedDescription = notebook.getDescription();
        if (request.description() != null) {
            updatedDescription = normalizeOptional(request.description());
        }

        notebook.update(updatedTitle, updatedDescription);
        return NotebookResponse.from(notebook);
    }

    @Transactional
    public NotebookDeleteResponse deleteNotebook(Long userId, Long notebookId) {
        Notebook notebook = findNotebookOrThrow(userId, notebookId);
        notebookRepository.delete(notebook);
        return new NotebookDeleteResponse(userId, notebookId, true);
    }

    @Transactional
    public NotebookItemResponse addNotebookItem(Long userId, Long notebookId, NotebookItemCreateRequest request) {
        Notebook notebook = findNotebookOrThrow(userId, notebookId);
        NotebookItemType itemType = request.itemType();
        Word word = resolveWordForCreate(notebookId, itemType, request.wordId());
        Integer sortOrder = request.sortOrder() != null ? request.sortOrder() : nextSortOrder(notebook);

        NotebookItem item = NotebookItem.create(
                itemType,
                word,
                normalizeRequired(request.expression(), "expression"),
                normalizeOptional(request.reading()),
                normalizeRequired(request.meaning(), "meaning"),
                normalizeOptional(request.memo()),
                sortOrder
        );
        notebook.addItem(item);
        notebookRepository.save(notebook);
        return NotebookItemResponse.from(item);
    }

    @Transactional
    public NotebookItemResponse updateNotebookItem(
            Long userId,
            Long notebookId,
            Long itemId,
            NotebookItemUpdateRequest request
    ) {
        findNotebookOrThrow(userId, notebookId);
        NotebookItem item = findNotebookItemOrThrow(notebookId, itemId);

        NotebookItemType targetItemType = request.itemType() != null ? request.itemType() : item.getItemType();
        Long currentWordId = item.getWord() != null ? item.getWord().getId() : null;
        Long targetWordId = request.wordId() != null ? request.wordId() : currentWordId;
        Word targetWord = resolveWordForUpdate(notebookId, itemId, targetItemType, targetWordId);

        String expression = request.expression() != null
                ? normalizeRequired(request.expression(), "expression")
                : item.getExpression();
        String reading = request.reading() != null ? normalizeOptional(request.reading()) : item.getReading();
        String meaning = request.meaning() != null
                ? normalizeRequired(request.meaning(), "meaning")
                : item.getMeaning();
        String memo = request.memo() != null ? normalizeOptional(request.memo()) : item.getMemo();
        Integer sortOrder = request.sortOrder() != null ? request.sortOrder() : item.getSortOrder();

        item.update(targetItemType, targetWord, expression, reading, meaning, memo, sortOrder);
        return NotebookItemResponse.from(item);
    }

    @Transactional
    public NotebookItemDeleteResponse deleteNotebookItem(Long userId, Long notebookId, Long itemId) {
        Notebook notebook = findNotebookOrThrow(userId, notebookId);
        NotebookItem item = findNotebookItemOrThrow(notebookId, itemId);
        notebook.removeItem(item);
        notebookRepository.save(notebook);
        return new NotebookItemDeleteResponse(userId, notebookId, itemId, true);
    }

    @Transactional
    public NotebookMigrationResponse migrateNotebooks(Long userId, NotebookMigrationRequest request) {
        User user = findUserOrThrow(userId);
        if (notebookRepository.countByUserId(userId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notebook migration is only supported when server notebooks are empty"
            );
        }

        int migratedCount = 0;
        for (NotebookMigrationNotebookRequest notebookRequest : request.notebooks()) {
            Notebook notebook = Notebook.create(
                    user,
                    normalizeRequired(notebookRequest.title(), "title"),
                    normalizeOptional(notebookRequest.description())
            );

            Set<Long> seenWordIds = new HashSet<>();
            List<NotebookMigrationItemRequest> items = notebookRequest.items() != null ? notebookRequest.items() : List.of();
            for (int index = 0; index < items.size(); index++) {
                NotebookMigrationItemRequest itemRequest = items.get(index);
                NotebookItemType itemType = itemRequest.wordId() != null ? NotebookItemType.WORD_REF : NotebookItemType.CUSTOM;
                Word word = null;
                if (itemType == NotebookItemType.WORD_REF) {
                    if (!seenWordIds.add(itemRequest.wordId())) {
                        continue;
                    }
                    word = findWordOrThrow(itemRequest.wordId());
                }

                notebook.addItem(NotebookItem.create(
                        itemType,
                        word,
                        normalizeRequired(itemRequest.expression(), "expression"),
                        normalizeOptional(itemRequest.reading()),
                        normalizeRequired(itemRequest.meaning(), "meaning"),
                        normalizeOptional(itemRequest.memo()),
                        index
                ));
            }

            notebookRepository.save(notebook);
            migratedCount += 1;
        }

        return new NotebookMigrationResponse(userId, migratedCount, request.notebooks().size());
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }

    private Notebook findNotebookOrThrow(Long userId, Long notebookId) {
        ensureUserExists(userId);
        return notebookRepository.findByIdAndUserId(notebookId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notebook not found: " + notebookId
                ));
    }

    private NotebookItem findNotebookItemOrThrow(Long notebookId, Long itemId) {
        return notebookItemRepository.findByIdAndNotebookId(itemId, notebookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notebook item not found: " + itemId
                ));
    }

    private Word resolveWordForCreate(Long notebookId, NotebookItemType itemType, Long wordId) {
        if (itemType == NotebookItemType.CUSTOM) {
            if (wordId != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CUSTOM item must not include wordId");
            }
            return null;
        }

        if (wordId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WORD_REF item requires wordId");
        }
        if (notebookItemRepository.existsByNotebookIdAndWordId(notebookId, wordId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "WORD_REF item already exists in notebook: " + wordId
            );
        }
        return findWordOrThrow(wordId);
    }

    private Word resolveWordForUpdate(Long notebookId, Long itemId, NotebookItemType itemType, Long wordId) {
        if (itemType == NotebookItemType.CUSTOM) {
            return null;
        }

        if (wordId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WORD_REF item requires wordId");
        }
        if (notebookItemRepository.existsByNotebookIdAndWordIdAndIdNot(notebookId, wordId, itemId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "WORD_REF item already exists in notebook: " + wordId
            );
        }
        return findWordOrThrow(wordId);
    }

    private Word findWordOrThrow(Long wordId) {
        return wordRepository.findById(wordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found: " + wordId));
    }

    private int nextSortOrder(Notebook notebook) {
        return notebook.getItems().stream()
                .map(NotebookItem::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(-1) + 1;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
