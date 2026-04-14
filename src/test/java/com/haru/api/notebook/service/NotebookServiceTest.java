package com.haru.api.notebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.haru.api.notebook.domain.Notebook;
import com.haru.api.notebook.domain.NotebookItem;
import com.haru.api.notebook.domain.NotebookItemType;
import com.haru.api.notebook.dto.NotebookCreateRequest;
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
import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.repository.WordRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private NotebookItemRepository notebookItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WordRepository wordRepository;

    private NotebookService notebookService;

    @BeforeEach
    void setUp() {
        notebookService = new NotebookService(notebookRepository, notebookItemRepository, userRepository, wordRepository);
    }

    @Test
    void createNotebook_savesNotebookForUser() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(notebookRepository.save(any(Notebook.class))).willAnswer(invocation -> {
            Notebook notebook = invocation.getArgument(0);
            setId(notebook, Notebook.class, 10L);
            return notebook;
        });

        NotebookResponse response = notebookService.createNotebook(
                1L,
                new NotebookCreateRequest("N2 단어장", "시험 대비")
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("N2 단어장");
        assertThat(response.description()).isEqualTo("시험 대비");
    }

    @Test
    void getNotebooks_returnsOrderedListForUser() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Notebook notebook = Notebook.create(user, "N2", "시험 대비");
        setId(notebook, Notebook.class, 11L);
        given(userRepository.existsById(1L)).willReturn(true);
        given(notebookRepository.findByUserIdOrderByCreatedAtAscIdAsc(1L)).willReturn(List.of(notebook));

        NotebookListResponse response = notebookService.getNotebooks(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.notebooks()).hasSize(1);
        assertThat(response.notebooks().get(0).id()).isEqualTo(11L);
        assertThat(response.notebooks().get(0).title()).isEqualTo("N2");
    }

    @Test
    void updateNotebook_updatesTitleAndDescription() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Notebook notebook = Notebook.create(user, "기존", "설명");
        setId(notebook, Notebook.class, 11L);
        given(userRepository.existsById(1L)).willReturn(true);
        given(notebookRepository.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(notebook));

        NotebookResponse response = notebookService.updateNotebook(
                1L,
                11L,
                new NotebookUpdateRequest("수정됨", "")
        );

        assertThat(response.title()).isEqualTo("수정됨");
        assertThat(response.description()).isNull();
    }

    @Test
    void deleteNotebook_deletesOwnedNotebook() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Notebook notebook = Notebook.create(user, "기존", null);
        setId(notebook, Notebook.class, 11L);
        given(userRepository.existsById(1L)).willReturn(true);
        given(notebookRepository.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(notebook));

        assertThat(notebookService.deleteNotebook(1L, 11L).deleted()).isTrue();
        verify(notebookRepository).delete(notebook);
    }

    @Test
    void addUpdateDeleteNotebookItem_handlesWordRefLifecycle() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Notebook notebook = Notebook.create(user, "단어장", null);
        setId(notebook, Notebook.class, 11L);
        Word word = new Word("花", "はな", WordLevel.N5);
        setId(word, Word.class, 123L);
        NotebookItem item = NotebookItem.create(NotebookItemType.WORD_REF, word, "花", "はな", "꽃", "메모", 0);
        setId(item, NotebookItem.class, 101L);
        notebook.addItem(item);

        given(userRepository.existsById(1L)).willReturn(true);
        given(notebookRepository.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(notebook));
        given(notebookItemRepository.existsByNotebookIdAndWordId(11L, 123L)).willReturn(false);
        given(wordRepository.findById(123L)).willReturn(Optional.of(word));
        given(notebookRepository.save(any(Notebook.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(notebookItemRepository.findByIdAndNotebookId(101L, 11L)).willReturn(Optional.of(item));
        given(notebookItemRepository.existsByNotebookIdAndWordIdAndIdNot(11L, 123L, 101L)).willReturn(false);

        NotebookItemResponse created = notebookService.addNotebookItem(
                1L,
                11L,
                new NotebookItemCreateRequest(NotebookItemType.WORD_REF, 123L, "花", "はな", "꽃", "메모", 0)
        );
        NotebookItemResponse updated = notebookService.updateNotebookItem(
                1L,
                11L,
                101L,
                new NotebookItemUpdateRequest(null, null, null, null, "꽃(수정)", "", 3)
        );
        NotebookItemDeleteResponse deleted = notebookService.deleteNotebookItem(1L, 11L, 101L);

        assertThat(created.itemType()).isEqualTo(NotebookItemType.WORD_REF);
        assertThat(updated.meaning()).isEqualTo("꽃(수정)");
        assertThat(updated.memo()).isNull();
        assertThat(updated.sortOrder()).isEqualTo(3);
        assertThat(deleted.deleted()).isTrue();
    }

    @Test
    void getNotebook_failsWhenOtherUserAccessesNotebook() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(notebookRepository.findByIdAndUserId(11L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notebookService.getNotebook(2L, 11L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Notebook not found: 11");
    }

    @Test
    void migrateNotebooks_savesPayloadWhenServerIsEmpty() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Word word = new Word("花", "はな", WordLevel.N5);
        setId(word, Word.class, 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(notebookRepository.countByUserId(1L)).willReturn(0L);
        given(wordRepository.findById(1L)).willReturn(Optional.of(word));
        given(notebookRepository.save(any(Notebook.class))).willAnswer(invocation -> invocation.getArgument(0));

        NotebookMigrationResponse response = notebookService.migrateNotebooks(
                1L,
                new NotebookMigrationRequest(List.of(
                        new NotebookMigrationNotebookRequest(
                                "단어장 이름",
                                "설명",
                                List.of(
                                        new NotebookMigrationItemRequest(1L, "花", "はな", "꽃", "메모"),
                                        new NotebookMigrationItemRequest(1L, "花", "はな", "꽃", "중복")
                                )
                        )
                ))
        );

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.migratedNotebookCount()).isEqualTo(1);
        assertThat(response.totalNotebookCount()).isEqualTo(1);
        verify(notebookRepository).save(any(Notebook.class));
    }

    @Test
    void addNotebookItem_blocksDuplicateWordRefInSameNotebook() {
        User user = new User(1L, "u1", WordLevel.N5, "CODE0001");
        Notebook notebook = Notebook.create(user, "단어장", null);
        setId(notebook, Notebook.class, 11L);
        given(userRepository.existsById(1L)).willReturn(true);
        given(notebookRepository.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(notebook));
        given(notebookItemRepository.existsByNotebookIdAndWordId(11L, 123L)).willReturn(true);

        assertThatThrownBy(() -> notebookService.addNotebookItem(
                1L,
                11L,
                new NotebookItemCreateRequest(NotebookItemType.WORD_REF, 123L, "花", "はな", "꽃", null, 0)
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("WORD_REF item already exists in notebook: 123");

        verify(wordRepository, never()).findById(123L);
    }

    private void setId(Object target, Class<?> type, Long id) {
        try {
            java.lang.reflect.Field field = type.getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
