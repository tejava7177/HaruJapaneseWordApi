package com.haru.api.notebook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.notebook.domain.NotebookItemType;
import com.haru.api.notebook.dto.NotebookDeleteResponse;
import com.haru.api.notebook.dto.NotebookItemDeleteResponse;
import com.haru.api.notebook.dto.NotebookItemResponse;
import com.haru.api.notebook.dto.NotebookListResponse;
import com.haru.api.notebook.dto.NotebookMigrationResponse;
import com.haru.api.notebook.dto.NotebookResponse;
import com.haru.api.notebook.service.NotebookService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(NotebookController.class)
class NotebookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotebookService notebookService;

    @Test
    void getNotebooks_returnsNotebookList() throws Exception {
        NotebookItemResponse item = new NotebookItemResponse(
                11L, NotebookItemType.WORD_REF, 123L, "花", "はな", "꽃", "메모", 0, null, null
        );
        NotebookResponse notebook = new NotebookResponse(
                1L,
                "N2 단어장",
                "시험 대비",
                LocalDateTime.of(2026, 4, 14, 10, 0),
                LocalDateTime.of(2026, 4, 14, 10, 5),
                List.of(item)
        );
        given(notebookService.getNotebooks(1L)).willReturn(new NotebookListResponse(1L, List.of(notebook)));

        mockMvc.perform(get("/api/users/1/notebooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.notebooks[0].id").value(1))
                .andExpect(jsonPath("$.notebooks[0].items[0].wordId").value(123));
    }

    @Test
    void createNotebook_returnsCreatedNotebook() throws Exception {
        given(notebookService.createNotebook(org.mockito.ArgumentMatchers.eq(1L), any()))
                .willReturn(new NotebookResponse(1L, "N2 단어장", "시험 대비", null, null, List.of()));

        mockMvc.perform(post("/api/users/1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "N2 단어장",
                                  "description": "시험 대비"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("N2 단어장"))
                .andExpect(jsonPath("$.description").value("시험 대비"));
    }

    @Test
    void updateNotebookItem_returnsUpdatedItem() throws Exception {
        given(notebookService.updateNotebookItem(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(2L), org.mockito.ArgumentMatchers.eq(11L), any()))
                .willReturn(new NotebookItemResponse(11L, NotebookItemType.CUSTOM, null, "꽃", null, "flower", null, 2, null, null));

        mockMvc.perform(patch("/api/users/1/notebooks/2/items/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemType": "CUSTOM",
                                  "expression": "꽃",
                                  "meaning": "flower",
                                  "sortOrder": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.itemType").value("CUSTOM"))
                .andExpect(jsonPath("$.sortOrder").value(2));
    }

    @Test
    void deleteEndpoints_returnSuccessPayloads() throws Exception {
        given(notebookService.deleteNotebook(1L, 2L)).willReturn(new NotebookDeleteResponse(1L, 2L, true));
        given(notebookService.deleteNotebookItem(1L, 2L, 11L))
                .willReturn(new NotebookItemDeleteResponse(1L, 2L, 11L, true));

        mockMvc.perform(delete("/api/users/1/notebooks/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));

        mockMvc.perform(delete("/api/users/1/notebooks/2/items/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(11))
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void migrateNotebooks_returnsCounts() throws Exception {
        given(notebookService.migrateNotebooks(org.mockito.ArgumentMatchers.eq(1L), any()))
                .willReturn(new NotebookMigrationResponse(1L, 1, 1));

        mockMvc.perform(post("/api/users/1/notebooks/migration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notebooks": [
                                    {
                                      "title": "단어장 이름",
                                      "description": "설명",
                                      "items": [
                                        {
                                          "wordId": 1,
                                          "expression": "花",
                                          "reading": "はな",
                                          "meaning": "꽃",
                                          "memo": "메모"
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.migratedNotebookCount").value(1))
                .andExpect(jsonPath("$.totalNotebookCount").value(1));
    }

    @Test
    void getNotebook_returnsNotFoundWhenNotebookMissing() throws Exception {
        given(notebookService.getNotebook(1L, 999L))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Notebook not found: 999"));

        mockMvc.perform(get("/api/users/1/notebooks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notebook not found: 999"));
    }
}
