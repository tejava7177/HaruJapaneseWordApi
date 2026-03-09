package com.haru.api.tsuntsun.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.service.TsunTsunService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TsunTsunController.class)
class TsunTsunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TsunTsunService tsunTsunService;

    @Test
    void sendTsunTsun_returnsQuiz() throws Exception {
        TsunTsunQuizResponse response = new TsunTsunQuizResponse(
                1L,
                1L,
                2L,
                10L,
                "ああ",
                "ああ",
                LocalDate.of(2026, 3, 10),
                TsunTsunStatus.SENT,
                List.of(new QuizChoiceResponse(100L, "인사"))
        );

        given(tsunTsunService.sendTsunTsun(1L, 2L, 11L)).willReturn(response);

        mockMvc.perform(post("/api/tsuntsun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senderId":1,"receiverId":2,"dailyWordItemId":11}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tsuntsunId").value(1))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void answerTsunTsun_returnsAnswerResult() throws Exception {
        TsunTsunAnswerResponse response = new TsunTsunAnswerResponse(1L, true, "인사", 100L, "인사", TsunTsunStatus.ANSWERED);
        given(tsunTsunService.answerTsunTsun(1L, 100L)).willReturn(response);

        mockMvc.perform(post("/api/tsuntsun/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tsuntsunId":1,"meaningId":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.status").value("ANSWERED"));
    }

    @Test
    void getToday_returnsList() throws Exception {
        given(tsunTsunService.getTodayTsunTsuns(2L)).willReturn(List.of());

        mockMvc.perform(get("/api/tsuntsun/today").param("userId", "2"))
                .andExpect(status().isOk());
    }
}
