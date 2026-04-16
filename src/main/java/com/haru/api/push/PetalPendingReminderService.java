package com.haru.api.push;

import com.haru.api.notification.service.NotificationHistoryService;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetalPendingReminderService {

    private final PetalPendingReminderTargetService petalPendingReminderTargetService;
    private final TsunTsunRepository tsunTsunRepository;
    private final UserDeviceTokenService userDeviceTokenService;
    private final NotificationHistoryService notificationHistoryService;
    private final PushNotificationService pushNotificationService;
    private final Clock clock;

    public void sendPendingReminders() {
        List<User> candidateUsers = petalPendingReminderTargetService.findCandidateUsers();
        log.info("[Push] petal pending reminder candidate user count={}", candidateUsers.size());

        LocalDate today = LocalDate.now(clock);
        for (User user : candidateUsers) {
            sendPendingReminder(user, today);
        }
    }

    void sendPendingReminder(User user, LocalDate today) {
        Long userId = user.getId();
        if (!hasPendingPetal(userId)) {
            log.info("[Push] petal pending reminder skipped userId={} reason=no_pending_petal", userId);
            return;
        }
        if (!user.isPetalNotificationsEnabled()) {
            log.info("[Push] petal pending reminder skipped userId={} reason=notifications_disabled", userId);
            return;
        }
        if (!userDeviceTokenService.hasActiveToken(userId)) {
            log.info("[Push] petal pending reminder skipped userId={} reason=no_device_token", userId);
            return;
        }
        if (notificationHistoryService.wasSentOnDate(userId, PushNotificationType.PETAL_PENDING_REMINDER, today)) {
            log.info("[Push] petal pending reminder skipped userId={} reason=already_sent_today", userId);
            return;
        }

        pushNotificationService.notifyPetalPendingReminder(user);
        if (!notificationHistoryService.recordSent(userId, PushNotificationType.PETAL_PENDING_REMINDER, today)) {
            log.info("[Push] petal pending reminder skipped userId={} reason=already_sent_today", userId);
            return;
        }
        log.info("[Push] petal pending reminder sent userId={}", userId);
    }

    private boolean hasPendingPetal(Long userId) {
        return tsunTsunRepository.countByReceiverIdAndStatus(userId, TsunTsunStatus.SENT) > 0;
    }
}
