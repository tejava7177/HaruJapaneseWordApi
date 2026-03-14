package com.haru.api.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdateRandomMatchingResponse;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.dto.UserProfileResponse;
import com.haru.api.user.service.UserService;
import com.haru.api.word.domain.WordLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUserProfile_returnsProfile() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                2L,
                "김민성",
                WordLevel.N2,
                "매일 한 문장씩 일본어 연습 중",
                "@minsung_jp",
                "8TR4XK6N",
                true
        );
        given(userService.getUserProfile(2L)).willReturn(response);

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.nickname").value("김민성"))
                .andExpect(jsonPath("$.learningLevel").value("N2"))
                .andExpect(jsonPath("$.bio").value("매일 한 문장씩 일본어 연습 중"))
                .andExpect(jsonPath("$.instagramId").value("@minsung_jp"))
                .andExpect(jsonPath("$.buddyCode").value("8TR4XK6N"))
                .andExpect(jsonPath("$.randomMatchingEnabled").value(true));
    }

    @Test
    void getUserProfile_returnsNotFoundWhenUserMissing() throws Exception {
        given(userService.getUserProfile(999L))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found: 999"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBuddyCode_returnsBuddyCode() throws Exception {
        UserBuddyCodeResponse response = new UserBuddyCodeResponse(1L, "JUHEUN01");
        given(userService.getBuddyCode(1L)).willReturn(response);

        mockMvc.perform(get("/api/users/1/buddy-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.buddyCode").value("JUHEUN01"));
    }

    @Test
    void getBuddyCode_returnsNotFoundWhenUserMissing() throws Exception {
        given(userService.getBuddyCode(999L))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found: 999"));

        mockMvc.perform(get("/api/users/999/buddy-code"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLearningLevel_returnsUpdatedUser() throws Exception {
        UpdateLearningLevelResponse response = new UpdateLearningLevelResponse(
                4L,
                "buddy4",
                WordLevel.N2,
                "Learning level updated. It will be applied to newly generated daily words."
        );
        given(userService.updateLearningLevel(4L, WordLevel.N2)).willReturn(response);

        mockMvc.perform(patch("/api/users/4/learning-level")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"learningLevel":"N2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(4))
                .andExpect(jsonPath("$.nickname").value("buddy4"))
                .andExpect(jsonPath("$.learningLevel").value("N2"));
    }

    @Test
    void updateLearningLevel_returnsBadRequestWhenLevelInvalid() throws Exception {
        mockMvc.perform(patch("/api/users/4/learning-level")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"learningLevel":"NX"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRandomMatching_returnsUpdatedUser() throws Exception {
        UpdateRandomMatchingResponse response = new UpdateRandomMatchingResponse(4L, true);
        given(userService.updateRandomMatchingEnabled(4L, true)).willReturn(response);

        mockMvc.perform(patch("/api/users/4/random-matching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(4))
                .andExpect(jsonPath("$.randomMatchingEnabled").value(true));
    }
}
