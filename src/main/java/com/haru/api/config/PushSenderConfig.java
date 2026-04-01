package com.haru.api.config;

import com.haru.api.push.ApnsPushSender;
import com.haru.api.push.NoopPushSender;
import com.haru.api.push.PushSender;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApnsProperties.class)
public class PushSenderConfig {

    @Bean
    public HttpClient apnsHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public PushSender pushSender(
            ApnsProperties apnsProperties,
            HttpClient apnsHttpClient,
            UserDeviceTokenService userDeviceTokenService
    ) {
        if (!apnsProperties.enabled()) {
            return new NoopPushSender();
        }

        if (!apnsProperties.hasRequiredSettings()) {
            log.warn("[Push] APNs sender disabled reason=missing_required_settings");
            return new NoopPushSender();
        }

        Path privateKeyPath = Path.of(apnsProperties.privateKeyPath());
        if (!Files.isRegularFile(privateKeyPath) || !Files.isReadable(privateKeyPath)) {
            log.warn("[Push] APNs sender disabled reason=private_key_unreadable path={}", privateKeyPath);
            return new NoopPushSender();
        }

        try {
            return new ApnsPushSender(apnsProperties, apnsHttpClient, userDeviceTokenService);
        } catch (RuntimeException exception) {
            log.warn("[Push] APNs sender disabled reason=initialization_failed message={}",
                    exception.getMessage(), exception);
            return new NoopPushSender();
        }
    }

    @Bean
    public ApplicationRunner pushSenderStartupLogger(PushSender pushSender, ApnsProperties apnsProperties) {
        return args -> {
            boolean keyPathLoaded = apnsProperties.hasRequiredSettings()
                    && Files.isRegularFile(Path.of(apnsProperties.privateKeyPath()))
                    && Files.isReadable(Path.of(apnsProperties.privateKeyPath()));
            log.info("[Push] active sender type={} apnsEnabled={} useSandbox={} keyPathLoaded={}",
                    pushSender.getClass().getSimpleName(),
                    apnsProperties.enabled(),
                    apnsProperties.useSandbox(),
                    keyPathLoaded);
        };
    }
}
