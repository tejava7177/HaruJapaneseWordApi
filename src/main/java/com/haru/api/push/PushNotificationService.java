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
            log.info("[Push] notify tsuntsun enter receiverId={} tsunTsunId={} senderUserId={} senderName={} notificationsEnabled={}",
                    targetUserId, tsunTsunId, senderUserId, senderName, null);
            log.info("[Push] skip tsuntsun reason=self_notification receiverId={} senderUserId={}", targetUserId, senderUserId);
            return;
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElse(null);
        Boolean notificationsEnabled = targetUser != null ? targetUser.isPetalNotificationsEnabled() : null;
        log.info("[Push] notify tsuntsun enter receiverId={} tsunTsunId={} senderUserId={} senderName={} notificationsEnabled={}",
                targetUserId, tsunTsunId, senderUserId, senderName, notificationsEnabled);

        if (targetUser == null) {
            log.info("[Push] skip tsuntsun reason=target_user_missing receiverId={}", targetUserId);
            return;
        }
        if (!targetUser.isPetalNotificationsEnabled()) {
            log.info("[Push] skip tsuntsun reason=petal_notifications_disabled receiverId={} notificationsEnabled={}",
                    targetUserId, targetUser.isPetalNotificationsEnabled());
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
                boolean hasAnyToken = userDeviceTokenService.hasAnyToken(targetUserId);
                boolean hasActiveToken = userDeviceTokenService.hasActiveToken(targetUserId);
                if (!hasAnyToken) {
                    log.info("[Push] skip {} reason=no_device_token receiverId={}", notificationType, targetUserId);
                } else if (!hasActiveToken) {
                    log.info("[Push] skip {} reason=token_inactive receiverId={}", notificationType, targetUserId);
                } else {
                    log.info("[Push] skip {} reason=no_active_token_after_lookup receiverId={}", notificationType, targetUserId);
                }
                return;
            }
            log.info("[Push] send attempt type={} receiverId={} tokenCount={} title={} data={}",
                    notificationType, targetUserId, deviceTokens.size(), title, data);
            pushSender.send(deviceTokens, title, body, data);
            log.info("[Push] send success type={} receiverId={} tokenCount={}",
                    notificationType, targetUserId, deviceTokens.size());
        } catch (RuntimeException exception) {
            log.warn("[Push] send failed type={} receiverId={} reason=sender_exception message={}",
                    notificationType, targetUserId, exception.getMessage(), exception);
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
