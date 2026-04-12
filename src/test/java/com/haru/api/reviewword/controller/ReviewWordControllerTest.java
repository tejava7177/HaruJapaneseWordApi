package com.haru.api.reviewword.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.reviewword.dto.ReviewWordListResponse;
import com.haru.api.reviewword.dto.ReviewWordMigrationResponse;
import com.haru.api.reviewword.dto.ReviewWordStatusResponse;
import com.haru.api.reviewword.service.ReviewWordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(ReviewWordController.class)
class ReviewWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewWordService reviewWordService;

    @Test
    void getReviewWords_returnsWordIdList() throws Exception {
        given(reviewWordService.getReviewWords(1L))
                .willReturn(new ReviewWordListResponse(1L, java.util.List.of(10L, 20L, 30L)));

        mockMvc.perform(get("/api/users/1/review-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.wordIds[0]").value(10))
                .andExpect(jsonPath("$.wordIds[1]").value(20))
                .andExpect(jsonPath("$.wordIds[2]").value(30));
    }

    @Test
    void addReviewWord_returnsReviewedTrue() throws Exception {
        given(reviewWordService.addReviewWord(1L, 10L))
                .willReturn(new ReviewWordStatusResponse(1L, 10L, true));

        mockMvc.perform(put("/api/users/1/review-words/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.wordId").value(10))
                .andExpect(jsonPath("$.reviewed").value(true));
    }

    @Test
    void removeReviewWord_returnsReviewedFalse() throws Exception {
        given(reviewWordService.removeReviewWord(1L, 10L))
                .willReturn(new ReviewWordStatusResponse(1L, 10L, false));

        mockMvc.perform(delete("/api/users/1/review-words/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.wordId").value(10))
                .andExpect(jsonPath("$.reviewed").value(false));
    }

    @Test
    void migrateReviewWords_returnsCounts() throws Exception {
        given(reviewWordService.migrateReviewWords(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .willReturn(new ReviewWordMigrationResponse(1L, 3, 5));

        mockMvc.perform(post("/api/users/1/review-words/migration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wordIds": [1, 2, 2, 3, 5]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.migratedCount").value(3))
                .andExpect(jsonPath("$.totalWordIds").value(5));
    }

    @Test
    void getReviewWords_returnsNotFoundWhenUserMissing() throws Exception {
        given(reviewWordService.getReviewWords(999L))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found: 999"));

        mockMvc.perform(get("/api/users/999/review-words"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }
}
