package com.haru.api.push;

import com.haru.api.config.ApnsProperties;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApnsPushSender implements PushSender {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final String APNS_PRODUCTION_URL = "https://api.push.apple.com";
    private static final String APNS_SANDBOX_URL = "https://api.sandbox.push.apple.com";
    private static final Duration PROVIDER_TOKEN_REFRESH_INTERVAL = Duration.ofMinutes(50);
    private static final Set<String> PERMANENT_FAILURE_REASONS = Set.of(
            "BadDeviceToken",
            "Unregistered",
            "DeviceTokenNotForTopic"
    );
    private static final Set<String> TEMPORARY_FAILURE_REASONS = Set.of(
            "TooManyProviderTokenUpdates",
            "TooManyRequests",
            "InternalServerError",
            "ServiceUnavailable"
    );

    private final ApnsProperties apnsProperties;
    private final HttpClient httpClient;
    private final UserDeviceTokenService userDeviceTokenService;
    private final PrivateKey privateKey;
    private final String apnsBaseUrl;
    private final Object providerTokenLock = new Object();
    private final AtomicReference<CachedProviderToken> cachedProviderToken = new AtomicReference<>();

    public ApnsPushSender(
            ApnsProperties apnsProperties,
            HttpClient httpClient,
            UserDeviceTokenService userDeviceTokenService
    ) {
        this.apnsProperties = apnsProperties;
        this.httpClient = httpClient;
        this.userDeviceTokenService = userDeviceTokenService;
        this.privateKey = loadPrivateKey(Path.of(apnsProperties.privateKeyPath()));
        this.apnsBaseUrl = apnsProperties.useSandbox() ? APNS_SANDBOX_URL : APNS_PRODUCTION_URL;
    }

    @Override
    public void send(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        int successCount = 0;
        int permanentFailureCount = 0;
        int temporaryFailureCount = 0;

        for (String deviceToken : deviceTokens) {
            log.info("[Push] APNs send attempt deviceToken={} topic={} sandbox={}",
                    abbreviateToken(deviceToken), apnsProperties.bundleId(), apnsProperties.useSandbox());
            try {
                HttpResponse<String> response = httpClient.send(buildRequest(deviceToken, title, body, data), HttpResponse.BodyHandlers.ofString());
                if (isSuccess(response.statusCode())) {
                    successCount++;
                    log.info("[Push] APNs send success deviceToken={} statusCode={}",
                            abbreviateToken(deviceToken), response.statusCode());
                    continue;
                }

                ApnsFailure failure = classifyFailure(response.statusCode(), extractReason(response.body()));
                if (failure.classification() == FailureClassification.PERMANENT) {
                    permanentFailureCount++;
                    userDeviceTokenService.disableToken(deviceToken, failure.reason());
                    log.warn("[Push] APNs send failed deviceToken={} statusCode={} reason={} classification={} tokenDisabled=true",
                            abbreviateToken(deviceToken), response.statusCode(), failure.reason(), failure.classification().logValue());
                } else {
                    temporaryFailureCount++;
                    log.warn("[Push] APNs send failed deviceToken={} statusCode={} reason={} classification={} tokenDisabled=false",
                            abbreviateToken(deviceToken), response.statusCode(), failure.reason(), failure.classification().logValue());
                }
            } catch (IOException | InterruptedException exception) {
                temporaryFailureCount++;
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.warn("[Push] APNs send failed deviceToken={} statusCode={} reason={} classification={} tokenDisabled=false",
                        abbreviateToken(deviceToken), "IO", exception.getMessage(),
                        FailureClassification.TEMPORARY.logValue(), exception);
            }
        }

        log.info("[Push] APNs send summary totalTokenCount={} successCount={} permanentFailureCount={} temporaryFailureCount={}",
                deviceTokens.size(), successCount, permanentFailureCount, temporaryFailureCount);

        if (successCount == 0 && (permanentFailureCount > 0 || temporaryFailureCount > 0)) {
            throw new IllegalStateException("APNs send failed for all device tokens");
        }
    }

    private HttpRequest buildRequest(String deviceToken, String title, String body, Map<String, String> data) {
        return HttpRequest.newBuilder()
                .uri(URI.create(apnsBaseUrl + "/3/device/" + deviceToken))
                .timeout(Duration.ofSeconds(10))
                .header("authorization", "bearer " + providerToken())
                .header("apns-topic", apnsProperties.bundleId())
                .header("apns-push-type", "alert")
                .header("apns-priority", "10")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildPayload(title, body, data), StandardCharsets.UTF_8))
                .build();
    }

    private String buildPayload(String title, String body, Map<String, String> data) {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"aps\":{");
        payload.append("\"alert\":{");
        payload.append("\"title\":\"").append(escapeJson(title)).append("\",");
        payload.append("\"body\":\"").append(escapeJson(body)).append("\"},");
        payload.append("\"sound\":\"default\"");
        payload.append("}");

        for (Map.Entry<String, String> entry : data.entrySet()) {
            payload.append(",\"")
                    .append(escapeJson(entry.getKey()))
                    .append("\":\"")
                    .append(escapeJson(entry.getValue()))
                    .append("\"");
        }
        payload.append("}");
        return payload.toString();
    }

    private String providerToken() {
        Instant now = Instant.now();
        CachedProviderToken currentToken = cachedProviderToken.get();
        if (currentToken != null && currentToken.isReusableAt(now)) {
            log.info("[Push] APNs reusing provider token issuedAt={}", currentToken.issuedAtEpochSecond());
            return currentToken.value();
        }

        synchronized (providerTokenLock) {
            CachedProviderToken refreshedCheck = cachedProviderToken.get();
            if (refreshedCheck != null && refreshedCheck.isReusableAt(now)) {
                log.info("[Push] APNs reusing provider token issuedAt={}", refreshedCheck.issuedAtEpochSecond());
                return refreshedCheck.value();
            }

            CachedProviderToken refreshed = createProviderToken(now);
            cachedProviderToken.set(refreshed);
            log.info("[Push] APNs refreshing provider token issuedAt={}", refreshed.issuedAtEpochSecond());
            return refreshed.value();
        }
    }

    private CachedProviderToken createProviderToken(Instant issuedAt) {
        long issuedAtEpochSecond = issuedAt.getEpochSecond();
        String header = "{\"alg\":\"ES256\",\"kid\":\"" + escapeJson(apnsProperties.keyId()) + "\"}";
        String payload = "{\"iss\":\"" + escapeJson(apnsProperties.teamId()) + "\",\"iat\":" + issuedAtEpochSecond + "}";
        String encodedHeader = encodeBase64Url(header);
        String encodedPayload = encodeBase64Url(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        return new CachedProviderToken(unsignedToken + "." + sign(unsignedToken), issuedAtEpochSecond);
    }

    private String sign(String unsignedToken) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(unsignedToken.getBytes(StandardCharsets.UTF_8));
            byte[] derSignature = signature.sign();
            return BASE64_URL_ENCODER.encodeToString(convertDerToJose(derSignature, 64));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to sign APNs JWT", exception);
        }
    }

    private PrivateKey loadPrivateKey(Path privateKeyPath) {
        try {
            String pem = Files.readString(privateKeyPath, StandardCharsets.UTF_8);
            String normalized = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(normalized);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("EC").generatePrivate(spec);
        } catch (IOException | GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to load APNs private key", exception);
        }
    }

    private byte[] convertDerToJose(byte[] derSignature, int joseLength) {
        if (derSignature.length < 8 || derSignature[0] != 0x30) {
            throw new IllegalStateException("Invalid DER signature format");
        }

        int offset = 2;
        if ((derSignature[1] & 0x80) != 0) {
            int lengthBytes = derSignature[1] & 0x7F;
            offset = 2 + lengthBytes;
        }

        if (derSignature[offset] != 0x02) {
            throw new IllegalStateException("Invalid DER signature R marker");
        }
        int rLength = derSignature[offset + 1] & 0xFF;
        byte[] r = new byte[rLength];
        System.arraycopy(derSignature, offset + 2, r, 0, rLength);

        int sOffset = offset + 2 + rLength;
        if (derSignature[sOffset] != 0x02) {
            throw new IllegalStateException("Invalid DER signature S marker");
        }
        int sLength = derSignature[sOffset + 1] & 0xFF;
        byte[] s = new byte[sLength];
        System.arraycopy(derSignature, sOffset + 2, s, 0, sLength);

        byte[] jose = new byte[joseLength];
        copyUnsigned(r, jose, 0, joseLength / 2);
        copyUnsigned(s, jose, joseLength / 2, joseLength / 2);
        return jose;
    }

    private void copyUnsigned(byte[] source, byte[] target, int targetOffset, int targetLength) {
        int sourceOffset = Math.max(0, source.length - targetLength);
        int length = Math.min(source.length, targetLength);
        System.arraycopy(source, sourceOffset, target, targetOffset + targetLength - length, length);
    }

    private String encodeBase64Url(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private ApnsFailure classifyFailure(int statusCode, String reason) {
        if (PERMANENT_FAILURE_REASONS.contains(reason)) {
            return new ApnsFailure(reason, FailureClassification.PERMANENT);
        }
        if (TEMPORARY_FAILURE_REASONS.contains(reason)) {
            return new ApnsFailure(reason, FailureClassification.TEMPORARY);
        }
        if (statusCode >= 500 || statusCode == 429) {
            return new ApnsFailure(reason, FailureClassification.TEMPORARY);
        }
        return new ApnsFailure(reason, FailureClassification.TEMPORARY);
    }

    private String extractReason(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "unknown";
        }

        String marker = "\"reason\":\"";
        int start = responseBody.indexOf(marker);
        if (start < 0) {
            return responseBody;
        }
        int reasonStart = start + marker.length();
        int reasonEnd = responseBody.indexOf('"', reasonStart);
        if (reasonEnd < 0) {
            return responseBody;
        }
        return responseBody.substring(reasonStart, reasonEnd);
    }

    private String abbreviateToken(String deviceToken) {
        if (deviceToken == null || deviceToken.length() <= 12) {
            return deviceToken;
        }
        return deviceToken.substring(0, 6) + "..." + deviceToken.substring(deviceToken.length() - 6);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private record CachedProviderToken(String value, long issuedAtEpochSecond) {
        private boolean isReusableAt(Instant now) {
            return now.isBefore(Instant.ofEpochSecond(issuedAtEpochSecond).plus(PROVIDER_TOKEN_REFRESH_INTERVAL));
        }
    }

    private record ApnsFailure(String reason, FailureClassification classification) {
    }

    private enum FailureClassification {
        PERMANENT,
        TEMPORARY;

        private String logValue() {
            return name().toLowerCase();
        }
    }
}
