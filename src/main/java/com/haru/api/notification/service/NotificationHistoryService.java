package com.haru.api.notification.service;

import com.haru.api.notification.domain.NotificationHistory;
import com.haru.api.notification.repository.NotificationHistoryRepository;
import com.haru.api.push.PushNotificationType;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationHistoryService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    public boolean wasSentOnDate(Long userId, PushNotificationType notificationType, LocalDate targetDate) {
        return notificationHistoryRepository.existsByUserIdAndNotificationTypeAndTargetDate(
                userId,
                notificationType,
                targetDate
        );
    }

    @Transactional
    public boolean recordSent(Long userId, PushNotificationType notificationType, LocalDate targetDate) {
        try {
            notificationHistoryRepository.saveAndFlush(NotificationHistory.of(userId, notificationType, targetDate));
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
    }
}
