package com.haru.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AppleIdentityTokenParserTest {

    private AppleIdentityTokenParser appleIdentityTokenParser;

    @BeforeEach
    void setUp() {
        appleIdentityTokenParser = new AppleIdentityTokenParser(new ObjectMapper());
    }

    @Test
    void parse_extractsSubjectAndEmail() {
        String identityToken = createToken("""
                {"iss":"https://appleid.apple.com","sub":"apple-subject-1","email":"juheun9912@naver.com","exp":%d}
                """.formatted(Instant.now().plusSeconds(300).getEpochSecond()));

        AppleIdentityTokenPayload payload = appleIdentityTokenParser.parse(identityToken);

        assertThat(payload.subject()).isEqualTo("apple-subject-1");
        assertThat(payload.email()).isEqualTo("juheun9912@naver.com");
    }

    @Test
    void parse_failsWhenSubjectMissing() {
        String identityToken = createToken("""
                {"iss":"https://appleid.apple.com","exp":%d}
                """.formatted(Instant.now().plusSeconds(300).getEpochSecond()));

        assertThatThrownBy(() -> appleIdentityTokenParser.parse(identityToken))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Failed to extract Apple subject");
    }

    @Test
    void parse_failsWhenTokenExpired() {
        String identityToken = createToken("""
                {"iss":"https://appleid.apple.com","sub":"apple-subject-1","exp":%d}
                """.formatted(Instant.now().minusSeconds(10).getEpochSecond()));

        assertThatThrownBy(() -> appleIdentityTokenParser.parse(identityToken))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Apple identity token is expired");
    }

    private String createToken(String payloadJson) {
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return encodedHeader + "." + encodedPayload + ".signature";
    }
}
