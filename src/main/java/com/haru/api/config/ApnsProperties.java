package com.haru.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apns")
public record ApnsProperties(
        boolean enabled,
        String keyId,
        String teamId,
        String bundleId,
        String privateKeyPath,
        boolean useSandbox
) {
    public boolean hasRequiredSettings() {
        return enabled
                && hasText(keyId)
                && hasText(teamId)
                && hasText(bundleId)
                && hasText(privateKeyPath);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
