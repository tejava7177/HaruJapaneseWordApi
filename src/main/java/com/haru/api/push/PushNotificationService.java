package com.haru.api.push;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final String DEFAULT_TITLE = "하루";

    private final UserRepository userRepository;
    private final UserDeviceTokenService userDeviceTokenService;
    private final PushSender pushSender;

    public void notifyBuddyRequestReceived(Long targetUserId, Long requestId, Long requesterId) {
        sendSafely(
                "buddy request",
                targetUserId,
                "새로운 버디 신청이 도착했어요",
                Map.of(
                        "type", "BUDDY_REQUEST_RECEIVED",
                        "requestId", String.valueOf(requestId),
                        "requesterId", String.valueOf(requesterId)
                )
        );
    }

    public void notifyBuddyAccepted(Long targetUserId, Long requestId, Long buddyUserId) {
        sendSafely(
                "buddy accepted",
                targetUserId,
                "버디 요청이 수락됐어요",
                Map.of(
                        "type", "BUDDY_ACCEPTED",
                        "requestId", String.valueOf(requestId),
                        "buddyUserId", String.valueOf(buddyUserId)
                )
        );
    }

    public void notifyTsunTsunReceived(Long targetUserId, Long tsunTsunId, Long senderUserId, String senderName) {
        if (targetUserId.equals(senderUserId)) {
            log.info("[Push] skip tsuntsun self notification targetUserId={} senderUserId={}", targetUserId, senderUserId);
            return;
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElse(null);
        if (targetUser == null) {
            log.info("[Push] skip tsuntsun target user missing targetUserId={}", targetUserId);
            return;
        }
        if (!targetUser.isPetalNotificationsEnabled()) {
            log.info("[Push] skip tsuntsun notifications disabled targetUserId={}", targetUserId);
            return;
        }

        sendSafely(
                "tsuntsun",
                targetUserId,
                "꽃잎이 도착했어요",
                senderName + "님이 꽃잎을 날렸어요",
                Map.of(
                        "type", "PETAL_RECEIVED",
                        "tsunTsunId", String.valueOf(tsunTsunId),
                        "senderUserId", String.valueOf(senderUserId)
                )
        );
    }

    private void sendSafely(String notificationType, Long targetUserId, String title, String body, Map<String, String> data) {
        try {
            List<String> deviceTokens = userDeviceTokenService.findActiveTokensByUserId(targetUserId);
            logByType(notificationType, targetUserId, deviceTokens.size());
            if (deviceTokens.isEmpty()) {
                return;
            }
            pushSender.send(deviceTokens, title, body, data);
        } catch (RuntimeException exception) {
            log.warn("[Push] send failed type={} targetUserId={} reason={}", notificationType, targetUserId, exception.getMessage(), exception);
        }
    }

    private void sendSafely(String notificationType, Long targetUserId, String body, Map<String, String> data) {
        sendSafely(notificationType, targetUserId, DEFAULT_TITLE, body, data);
    }

    private void logByType(String notificationType, Long targetUserId, int tokenCount) {
        switch (notificationType) {
            case "buddy request" -> log.info("[Push] notify buddy request targetUserId={} tokenCount={}", targetUserId, tokenCount);
            case "buddy accepted" -> log.info("[Push] notify buddy accepted targetUserId={} tokenCount={}", targetUserId, tokenCount);
            case "tsuntsun" -> log.info("[Push] notify tsuntsun targetUserId={} tokenCount={}", targetUserId, tokenCount);
            default -> log.info("[Push] notify type={} targetUserId={} tokenCount={}", notificationType, targetUserId, tokenCount);
        }
    }
}
