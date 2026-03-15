package com.haru.api.buddy.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.buddy.domain.BuddyRequestStatus;
import com.haru.api.buddy.dto.BuddyRequestActionResponse;
import com.haru.api.buddy.service.BuddyRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(BuddyRequestController.class)
class BuddyRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuddyRequestService buddyRequestService;

    @Test
    void createRequest_returnsCreatedRequest() throws Exception {
        given(buddyRequestService.createRequest(1L, 2L))
                .willReturn(new BuddyRequestActionResponse(10L, BuddyRequestStatus.PENDING));

        mockMvc.perform(post("/api/buddy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requesterId":1,"targetUserId":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createRequest_returnsDetailedBadRequestMessage() throws Exception {
        given(buddyRequestService.createRequest(1L, 2L))
                .willThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "상대가 랜덤 매칭 노출을 꺼두었습니다."
                ));

        mockMvc.perform(post("/api/buddy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requesterId":1,"targetUserId":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("상대가 랜덤 매칭 노출을 꺼두었습니다."));
    }
}
