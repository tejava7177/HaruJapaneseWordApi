package com.haru.api.config;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ProfileImageStorageProperties.class)
public class ProfileImageWebConfig implements WebMvcConfigurer {

    private final ProfileImageStorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(properties.storagePath()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(properties.urlPrefix() + "/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/");
    }
}
