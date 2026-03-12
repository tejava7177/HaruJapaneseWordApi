package com.haru.api.tsuntsun.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.dto.QuizChoiceResponse;
import com.haru.api.tsuntsun.dto.TsunTsunAnswerResponse;
import com.haru.api.tsuntsun.dto.TsunTsunDirection;
import com.haru.api.tsuntsun.dto.TsunTsunInboxItemResponse;
import com.haru.api.tsuntsun.dto.TsunTsunInboxResponse;
import com.haru.api.tsuntsun.dto.TsunTsunQuizResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayItemResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayResponse;
import com.haru.api.tsuntsun.dto.TsunTsunTodayStatus;
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
        TsunTsunAnswerResponse response = new TsunTsunAnswerResponse(1L, true, 100L, "인사", 100L, "인사", TsunTsunStatus.ANSWERED);
        given(tsunTsunService.answerTsunTsun(1L, 100L)).willReturn(response);

        mockMvc.perform(post("/api/tsuntsun/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tsuntsunId":1,"meaningId":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.selectedMeaningId").value(100))
                .andExpect(jsonPath("$.correctText").value("인사"))
                .andExpect(jsonPath("$.status").value("ANSWERED"));
    }

    @Test
    void getInbox_returnsUnansweredItems() throws Exception {
        TsunTsunInboxResponse response = new TsunTsunInboxResponse(
                2L,
                1,
                List.of(new TsunTsunInboxItemResponse(
                        11L,
                        1L,
                        "김민성",
                        390L,
                        "紹介",
                        "しょうかい",
                        LocalDate.of(2026, 3, 12),
                        List.of(new QuizChoiceResponse(100L, "소개"), new QuizChoiceResponse(-1L, "모르겠어요"))
                ))
        );
        given(tsunTsunService.getInbox(2L)).willReturn(response);

        mockMvc.perform(get("/api/tsuntsun/inbox").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.unansweredCount").value(1))
                .andExpect(jsonPath("$.items[0].senderName").value("김민성"))
                .andExpect(jsonPath("$.items[0].choices[0].meaningId").value(100));
    }

    @Test
    void getToday_returnsPairStatus() throws Exception {
        TsunTsunTodayResponse response = new TsunTsunTodayResponse(
                1L,
                2L,
                LocalDate.of(2026, 3, 10),
                3,
                2,
                List.of(new TsunTsunTodayItemResponse(101L, 201L, TsunTsunDirection.SENT, TsunTsunTodayStatus.ANSWERED))
        );

        given(tsunTsunService.getTodayTsunTsuns(1L, 2L)).willReturn(response);

        mockMvc.perform(get("/api/tsuntsun/today").param("userId", "1").param("buddyId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(3))
                .andExpect(jsonPath("$.receivedCount").value(2))
                .andExpect(jsonPath("$.items[0].direction").value("SENT"));
    }
}
