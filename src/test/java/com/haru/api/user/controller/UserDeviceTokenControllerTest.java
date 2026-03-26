package com.haru.api.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haru.api.userdevice.domain.DevicePlatform;
import com.haru.api.userdevice.dto.RegisterDeviceTokenResponse;
import com.haru.api.userdevice.dto.UnregisterDeviceTokenResponse;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(UserDeviceTokenController.class)
class UserDeviceTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDeviceTokenService userDeviceTokenService;

    @Test
    void registerDeviceToken_returnsRegisteredToken() throws Exception {
        RegisterDeviceTokenResponse response =
                new RegisterDeviceTokenResponse(12L, "token-1", DevicePlatform.IOS, true, true);
        given(userDeviceTokenService.registerToken(org.mockito.ArgumentMatchers.eq(12L), org.mockito.ArgumentMatchers.any()))
                .willReturn(response);

        mockMvc.perform(post("/api/users/12/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceToken": "token-1",
                                  "platform": "IOS",
                                  "pushEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(12))
                .andExpect(jsonPath("$.deviceToken").value("token-1"))
                .andExpect(jsonPath("$.platform").value("IOS"))
                .andExpect(jsonPath("$.pushEnabled").value(true))
                .andExpect(jsonPath("$.registered").value(true));
    }

    @Test
    void unregisterDeviceToken_returnsSuccessResponse() throws Exception {
        UnregisterDeviceTokenResponse response = new UnregisterDeviceTokenResponse(12L, "token-1", false, true);
        given(userDeviceTokenService.unregisterToken(12L, "token-1")).willReturn(response);

        mockMvc.perform(delete("/api/users/12/devices/token-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(12))
                .andExpect(jsonPath("$.deviceToken").value("token-1"))
                .andExpect(jsonPath("$.pushEnabled").value(false))
                .andExpect(jsonPath("$.unregistered").value(true));
    }

    @Test
    void registerDeviceToken_returnsBadRequestWhenBodyMalformed() throws Exception {
        mockMvc.perform(post("/api/users/12/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceToken":
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void unregisterDeviceToken_returnsNotFoundWhenUserMissing() throws Exception {
        given(userDeviceTokenService.unregisterToken(12L, "token-1"))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found: 12"));

        mockMvc.perform(delete("/api/users/12/devices/token-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 12"));
    }
}
