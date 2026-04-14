package com.haru.api.notebook.controller;

import com.haru.api.notebook.dto.NotebookCreateRequest;
import com.haru.api.notebook.dto.NotebookDeleteResponse;
import com.haru.api.notebook.dto.NotebookItemCreateRequest;
import com.haru.api.notebook.dto.NotebookItemDeleteResponse;
import com.haru.api.notebook.dto.NotebookItemResponse;
import com.haru.api.notebook.dto.NotebookItemUpdateRequest;
import com.haru.api.notebook.dto.NotebookListResponse;
import com.haru.api.notebook.dto.NotebookMigrationRequest;
import com.haru.api.notebook.dto.NotebookMigrationResponse;
import com.haru.api.notebook.dto.NotebookResponse;
import com.haru.api.notebook.dto.NotebookUpdateRequest;
import com.haru.api.notebook.service.NotebookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/notebooks")
@RequiredArgsConstructor
@Tag(name = "Notebooks")
public class NotebookController {

    private final NotebookService notebookService;

    @GetMapping
    @Operation(summary = "사용자 단어장 목록 조회")
    public NotebookListResponse getNotebooks(@PathVariable Long userId) {
        return notebookService.getNotebooks(userId);
    }

    @PostMapping
    @Operation(summary = "단어장 생성")
    public NotebookResponse createNotebook(
            @PathVariable Long userId,
            @Valid @RequestBody NotebookCreateRequest request
    ) {
        return notebookService.createNotebook(userId, request);
    }

    @GetMapping("/{notebookId}")
    @Operation(summary = "단어장 상세 조회")
    public NotebookResponse getNotebook(@PathVariable Long userId, @PathVariable Long notebookId) {
        return notebookService.getNotebook(userId, notebookId);
    }

    @PatchMapping("/{notebookId}")
    @Operation(summary = "단어장 수정")
    public NotebookResponse updateNotebook(
            @PathVariable Long userId,
            @PathVariable Long notebookId,
            @RequestBody NotebookUpdateRequest request
    ) {
        return notebookService.updateNotebook(userId, notebookId, request);
    }

    @DeleteMapping("/{notebookId}")
    @Operation(summary = "단어장 삭제")
    public NotebookDeleteResponse deleteNotebook(@PathVariable Long userId, @PathVariable Long notebookId) {
        return notebookService.deleteNotebook(userId, notebookId);
    }

    @PostMapping("/{notebookId}/items")
    @Operation(summary = "단어장 아이템 추가")
    public NotebookItemResponse addNotebookItem(
            @PathVariable Long userId,
            @PathVariable Long notebookId,
            @Valid @RequestBody NotebookItemCreateRequest request
    ) {
        return notebookService.addNotebookItem(userId, notebookId, request);
    }

    @PatchMapping("/{notebookId}/items/{itemId}")
    @Operation(summary = "단어장 아이템 수정")
    public NotebookItemResponse updateNotebookItem(
            @PathVariable Long userId,
            @PathVariable Long notebookId,
            @PathVariable Long itemId,
            @RequestBody NotebookItemUpdateRequest request
    ) {
        return notebookService.updateNotebookItem(userId, notebookId, itemId, request);
    }

    @DeleteMapping("/{notebookId}/items/{itemId}")
    @Operation(summary = "단어장 아이템 삭제")
    public NotebookItemDeleteResponse deleteNotebookItem(
            @PathVariable Long userId,
            @PathVariable Long notebookId,
            @PathVariable Long itemId
    ) {
        return notebookService.deleteNotebookItem(userId, notebookId, itemId);
    }

    @PostMapping("/migration")
    @Operation(summary = "로컬 단어장 서버 마이그레이션")
    public NotebookMigrationResponse migrateNotebooks(
            @PathVariable Long userId,
            @Valid @RequestBody NotebookMigrationRequest request
    ) {
        return notebookService.migrateNotebooks(userId, request);
    }
}
