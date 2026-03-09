package com.haru.api.dailyword.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.dailyword.dto.DailyWordItemResponse;
import com.haru.api.dailyword.dto.DailyWordTodayResponse;
import com.haru.api.dailyword.service.DailyWordService;
import com.haru.api.word.domain.WordLevel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DailyWordController.class)
class DailyWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DailyWordService dailyWordService;

    @Test
    void getTodayWords_returnsDailyWords() throws Exception {
        DailyWordTodayResponse response = new DailyWordTodayResponse(
                1L,
                LocalDate.of(2026, 3, 9),
                WordLevel.N4,
                List.of(new DailyWordItemResponse(1L, "ああ", "ああ", WordLevel.N4, 1))
        );

        given(dailyWordService.getTodayWords(1L)).willReturn(response);

        mockMvc.perform(get("/api/daily-words/today").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.targetDate").value("2026-03-09"))
                .andExpect(jsonPath("$.level").value("N4"))
                .andExpect(jsonPath("$.items[0].wordId").value(1))
                .andExpect(jsonPath("$.items[0].orderIndex").value(1));
    }
}
