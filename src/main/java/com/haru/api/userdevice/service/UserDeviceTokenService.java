package com.haru.api.userdevice.service;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.userdevice.domain.UserDeviceToken;
import com.haru.api.userdevice.dto.RegisterDeviceTokenRequest;
import com.haru.api.userdevice.dto.RegisterDeviceTokenResponse;
import com.haru.api.userdevice.dto.UnregisterDeviceTokenResponse;
import com.haru.api.userdevice.repository.UserDeviceTokenRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDeviceTokenService {

    private final UserRepository userRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Transactional
    public RegisterDeviceTokenResponse registerToken(Long userId, RegisterDeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        String normalizedToken = normalizeDeviceToken(request.deviceToken());

        UserDeviceToken userDeviceToken = userDeviceTokenRepository.findByDeviceToken(normalizedToken)
                .map(existingToken -> {
                    existingToken.register(user, request.platform(), request.pushEnabled());
                    return existingToken;
                })
                .orElseGet(() -> UserDeviceToken.create(user, normalizedToken, request.platform(), request.pushEnabled()));

        UserDeviceToken savedToken = userDeviceTokenRepository.save(userDeviceToken);
        log.info("[Push] register device token userId={} platform={}", userId, savedToken.getPlatform());
        return RegisterDeviceTokenResponse.from(savedToken);
    }

    @Transactional
    public UnregisterDeviceTokenResponse unregisterToken(Long userId, String deviceToken) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }

        String normalizedToken = normalizeDeviceToken(deviceToken);

        return userDeviceTokenRepository.findByUserIdAndDeviceToken(userId, normalizedToken)
                .map(existingToken -> {
                    existingToken.unregister();
                    log.info("[Push] unregister device token userId={}", userId);
                    return new UnregisterDeviceTokenResponse(userId, normalizedToken, false, true);
                })
                .orElseGet(() -> {
                    log.info("[Push] unregister device token userId={} skipped=not_found", userId);
                    return new UnregisterDeviceTokenResponse(userId, normalizedToken, false, true);
                });
    }

    public List<String> findActiveTokensByUserId(Long userId) {
        return userDeviceTokenRepository.findByUserIdAndPushEnabledTrue(userId).stream()
                .map(UserDeviceToken::getDeviceToken)
                .toList();
    }

    public boolean hasAnyToken(Long userId) {
        return userDeviceTokenRepository.existsByUserId(userId);
    }

    public boolean hasActiveToken(Long userId) {
        return userDeviceTokenRepository.existsByUserIdAndPushEnabledTrue(userId);
    }

    @Transactional
    public void disableToken(String deviceToken, String reason) {
        String normalizedToken = normalizeDeviceToken(deviceToken);

        userDeviceTokenRepository.findByDeviceToken(normalizedToken)
                .ifPresentOrElse(existingToken -> {
                    existingToken.unregister();
                    log.info("[Push] disable device token token={} reason={} userId={}",
                            abbreviateToken(normalizedToken), reason, existingToken.getUser().getId());
                }, () -> log.info("[Push] disable device token skipped=not_found token={} reason={}",
                        abbreviateToken(normalizedToken), reason));
    }

    private String normalizeDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceToken is required");
        }
        return deviceToken.trim();
    }

    private String abbreviateToken(String deviceToken) {
        if (deviceToken.length() <= 12) {
            return deviceToken;
        }
        return deviceToken.substring(0, 6) + "..." + deviceToken.substring(deviceToken.length() - 6);
    }
}
