package com.haru.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.profile-image")
public record ProfileImageStorageProperties(
        String storagePath,
        String urlPrefix
) {
}
