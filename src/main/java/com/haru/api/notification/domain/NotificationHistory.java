package com.haru.api.notification.domain;

import com.haru.api.push.PushNotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(
        name = "notification_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_history_user_type_target_date",
                        columnNames = {"user_id", "notification_type", "target_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private PushNotificationType notificationType;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private NotificationHistory(Long userId, PushNotificationType notificationType, LocalDate targetDate) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.targetDate = targetDate;
    }

    public static NotificationHistory of(Long userId, PushNotificationType notificationType, LocalDate targetDate) {
        return new NotificationHistory(userId, notificationType, targetDate);
    }
}
