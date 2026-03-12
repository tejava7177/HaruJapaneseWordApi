package com.haru.api.dailyword.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.dailyword.dto.DevDailyWordRegenerateResponse;
import com.haru.api.dailyword.service.DailyWordService;
import com.haru.api.word.domain.WordLevel;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DevDailyWordController.class)
class DevDailyWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DailyWordService dailyWordService;

    @Test
    void regenerateTodayWords_returnsRegeneratedResponse() throws Exception {
        DevDailyWordRegenerateResponse response = new DevDailyWordRegenerateResponse(
                4L,
                LocalDate.of(2026, 3, 12),
                WordLevel.N2,
                10,
                "Today's daily words and related tsuntsun records were reset for development."
        );
        given(dailyWordService.regenerateTodayWordsForDevelopment(4L)).willReturn(response);

        mockMvc.perform(post("/api/dev/daily-words/4/regenerate-today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(4))
                .andExpect(jsonPath("$.targetDate").value("2026-03-12"))
                .andExpect(jsonPath("$.level").value("N2"))
                .andExpect(jsonPath("$.itemCount").value(10));
    }
}
