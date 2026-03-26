package com.haru.api.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AppleIdentityTokenParser {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final ObjectMapper objectMapper;

    public AppleIdentityTokenPayload parse(String identityToken) {
        if (identityToken == null || identityToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "identityToken is required");
        }

        String[] tokenParts = identityToken.split("\\.");
        if (tokenParts.length < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse Apple identity token");
        }

        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(tokenParts[1]);
            JsonNode payload = objectMapper.readTree(new String(decodedPayload, StandardCharsets.UTF_8));

            String issuer = payload.path("iss").asText(null);
            if (issuer != null && !APPLE_ISSUER.equals(issuer)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Apple identity token issuer");
            }

            long expiresAt = payload.path("exp").asLong(0L);
            if (expiresAt > 0 && Instant.ofEpochSecond(expiresAt).isBefore(Instant.now())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple identity token is expired");
            }

            String subject = payload.path("sub").asText(null);
            if (subject == null || subject.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to extract Apple subject");
            }

            String email = payload.path("email").asText(null);
            return new AppleIdentityTokenPayload(subject, email);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse Apple identity token");
        }
    }
}
