package com.haru.api.word.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.word.domain.WordLevel;
import com.haru.api.word.dto.WordDetailResponse;
import com.haru.api.word.dto.WordMeaningResponse;
import com.haru.api.word.dto.WordSimpleResponse;
import com.haru.api.word.service.WordService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(WordController.class)
class WordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WordService wordService;

    @Test
    void getWord_returnsDetail() throws Exception {
        WordDetailResponse response = new WordDetailResponse(
                1L,
                "ああ",
                "ああ",
                WordLevel.N4,
                List.of(new WordMeaningResponse(1L, "저렇게", 1))
        );
        given(wordService.getWord(1L)).willReturn(response);

        mockMvc.perform(get("/api/words/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.expression").value("ああ"))
                .andExpect(jsonPath("$.reading").value("ああ"))
                .andExpect(jsonPath("$.level").value("N4"))
                .andExpect(jsonPath("$.meanings[0].text").value("저렇게"));
    }

    @Test
    void getWord_whenMissing_returnsNotFound() throws Exception {
        given(wordService.getWord(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found: 999"));

        mockMvc.perform(get("/api/words/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWords_withLevel_returnsFilteredList() throws Exception {
        given(wordService.getWords(WordLevel.N4))
                .willReturn(List.of(new WordSimpleResponse(1L, "ああ", "ああ", WordLevel.N4)));

        mockMvc.perform(get("/api/words").param("level", "N4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].level").value("N4"));
    }
}
