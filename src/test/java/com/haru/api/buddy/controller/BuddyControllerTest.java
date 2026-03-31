package com.haru.api.buddy.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.buddy.domain.BuddyStatus;
import com.haru.api.buddy.dto.BuddyResponse;
import com.haru.api.buddy.service.BuddyService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BuddyController.class)
class BuddyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuddyService buddyService;

    @Test
    void getBuddies_includesLastActiveAtWithExpectedFieldName() throws Exception {
        BuddyResponse response = new BuddyResponse(
                100L,
                1L,
                2L,
                "buddy",
                BuddyStatus.ACTIVE,
                3L,
                LocalDateTime.of(2026, 3, 31, 10, 15, 30),
                true,
                LocalDateTime.of(2026, 3, 31, 9, 0),
                LocalDateTime.of(2026, 3, 31, 10, 0)
        );
        given(buddyService.getBuddies(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/buddies").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buddyUserId").value(2))
                .andExpect(jsonPath("$[0].lastActiveAt").value("2026-03-31T10:15:30"))
                .andExpect(jsonPath("$[0].hasUnreadPetal").value(true));
    }

    @Test
    void getBuddies_keepsLastActiveAtFieldEvenWhenNull() throws Exception {
        BuddyResponse response = new BuddyResponse(
                100L,
                1L,
                2L,
                "buddy",
                BuddyStatus.ACTIVE,
                3L,
                null,
                false,
                null,
                null
        );
        given(buddyService.getBuddies(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/buddies").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastActiveAt").isEmpty());
    }
}
