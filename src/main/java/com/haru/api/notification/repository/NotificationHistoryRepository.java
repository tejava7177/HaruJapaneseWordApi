package com.haru.api.notification.repository;

import com.haru.api.notification.domain.NotificationHistory;
import com.haru.api.push.PushNotificationType;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    boolean existsByUserIdAndNotificationTypeAndTargetDate(
            Long userId,
            PushNotificationType notificationType,
            LocalDate targetDate
    );
}
