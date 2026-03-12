package com.haru.api.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.service.UserService;
import com.haru.api.word.domain.WordLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
}
