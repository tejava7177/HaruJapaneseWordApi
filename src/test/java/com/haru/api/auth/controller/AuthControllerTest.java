package com.haru.api.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.auth.dto.AppleAuthResponse;
import com.haru.api.auth.service.AppleAuthService;
import com.haru.api.word.domain.WordLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppleAuthService appleAuthService;

    @Test
    void authenticateApple_returnsOfficialServerUserId() throws Exception {
        AppleAuthResponse response = new AppleAuthResponse(
                12L,
                "000123.abcdeffedcba.1234",
                "심주흔",
                WordLevel.N3,
                "juheun9912@naver.com",
                "심주흔",
                false,
                null
        );
        given(appleAuthService.authenticate(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(post("/api/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityToken": "token",
                                  "appleUserId": "000123.abcdeffedcba.1234",
                                  "email": "juheun9912@naver.com",
                                  "displayName": "심주흔"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(12))
                .andExpect(jsonPath("$.appleUserId").value("000123.abcdeffedcba.1234"))
                .andExpect(jsonPath("$.nickname").value("심주흔"))
                .andExpect(jsonPath("$.learningLevel").value("N3"))
                .andExpect(jsonPath("$.email").value("juheun9912@naver.com"))
                .andExpect(jsonPath("$.displayName").value("심주흔"))
                .andExpect(jsonPath("$.isNewUser").value(false));
    }

    @Test
    void authenticateApple_returnsBadRequestWithConsistentErrorWhenRequestBodyMalformed() throws Exception {
        mockMvc.perform(post("/api/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityToken":
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.path").value("/api/auth/apple"));
    }

    @Test
    void authenticateApple_returnsNotFoundStyleErrorPayloadWhenServiceFails() throws Exception {
        given(appleAuthService.authenticate(org.mockito.ArgumentMatchers.any()))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Failed to extract Apple subject"));

        mockMvc.perform(post("/api/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityToken": "bad-token"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Failed to extract Apple subject"))
                .andExpect(jsonPath("$.path").value("/api/auth/apple"));
    }
}
