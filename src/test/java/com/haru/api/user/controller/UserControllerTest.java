package com.haru.api.user.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdatePetalNotificationsResponse;
import com.haru.api.user.dto.UpdateProfileImageResponse;
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
import org.springframework.mock.web.MockMultipartFile;
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
                true,
                true,
                "https://cdn.haru.app/profiles/2.png"
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
                .andExpect(jsonPath("$.randomMatchingEnabled").value(true))
                .andExpect(jsonPath("$.petalNotificationsEnabled").value(true))
                .andExpect(jsonPath("$.profileImageUrl").value("https://cdn.haru.app/profiles/2.png"));
    }

    @Test
    void getUserProfile_returnsNullProfileImageUrlWhenMissing() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                3L,
                "하루",
                WordLevel.N3,
                "소개",
                "@haru_jp",
                "HARU0003",
                false,
                true,
                null
        );
        given(userService.getUserProfile(3L)).willReturn(response);

        mockMvc.perform(get("/api/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.profileImageUrl").isEmpty());
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

    @Test
    void updatePetalNotifications_returnsUpdatedUser() throws Exception {
        UpdatePetalNotificationsResponse response = new UpdatePetalNotificationsResponse(4L, false);
        given(userService.updatePetalNotificationsEnabled(4L, false)).willReturn(response);

        mockMvc.perform(patch("/api/users/4/petal-notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(4))
                .andExpect(jsonPath("$.petalNotificationsEnabled").value(false));
    }

    @Test
    void updateUserProfile_returnsUpdatedProfile() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "심주흔",
                WordLevel.N2,
                "매일 한 문장씩 일본어 연습 중",
                "@minsung_jp",
                "4C4AFKN2",
                false,
                true,
                null
        );
        given(userService.updateUserProfile(1L, "심주흔", "매일 한 문장씩 일본어 연습 중", "@minsung_jp"))
                .willReturn(response);

        mockMvc.perform(patch("/api/users/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"심주흔",
                                  "bio":"매일 한 문장씩 일본어 연습 중",
                                  "instagramId":"@minsung_jp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.nickname").value("심주흔"))
                .andExpect(jsonPath("$.bio").value("매일 한 문장씩 일본어 연습 중"))
                .andExpect(jsonPath("$.instagramId").value("@minsung_jp"))
                .andExpect(jsonPath("$.buddyCode").value("4C4AFKN2"))
                .andExpect(jsonPath("$.randomMatchingEnabled").value(false));
    }

    @Test
    void updateUserProfile_returnsBadRequestWhenNicknameTooLong() throws Exception {
        mockMvc.perform(patch("/api/users/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"1234567890123456789012345678901"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("nickname: must be at most 30 characters"));
    }

    @Test
    void updateUserProfile_returnsBadRequestWhenNicknameBlank() throws Exception {
        given(userService.updateUserProfile(1L, "   ", null, null))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "nickname must not be blank"));

        mockMvc.perform(patch("/api/users/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("nickname must not be blank"));
    }

    @Test
    void updateUserProfile_returnsNotFoundWhenUserMissing() throws Exception {
        given(userService.updateUserProfile(999L, "심주흔", null, null))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found: 999"));

        mockMvc.perform(patch("/api/users/999/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"심주흔"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }

    @Test
    void uploadProfileImage_returnsUpdatedProfileImageUrl() throws Exception {
        UpdateProfileImageResponse response =
                new UpdateProfileImageResponse(2L, "/uploads/profile/2-1234.png");
        MockMultipartFile file =
                new MockMultipartFile("file", "profile.png", "image/png", "image".getBytes());
        given(userService.uploadProfileImage(eq(2L), any())).willReturn(response);

        mockMvc.perform(multipart("/api/users/2/profile-image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.profileImageUrl").value("/uploads/profile/2-1234.png"));
    }

    @Test
    void uploadProfileImage_returnsNotFoundWhenUserMissing() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "profile.png", "image/png", "image".getBytes());
        given(userService.uploadProfileImage(eq(999L), any()))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found: 999"));

        mockMvc.perform(multipart("/api/users/999/profile-image")
                        .file(file))
                .andExpect(status().isNotFound());
    }
}
