package com.haru.api.push;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.haru.api.notification.service.NotificationHistoryService;
import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import com.haru.api.word.domain.WordLevel;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetalPendingReminderServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private PetalPendingReminderTargetService petalPendingReminderTargetService;

    @Mock
    private TsunTsunRepository tsunTsunRepository;

    @Mock
    private UserDeviceTokenService userDeviceTokenService;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @Mock
    private PushNotificationService pushNotificationService;

    private Clock clock;
    private PetalPendingReminderService petalPendingReminderService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(OffsetDateTime.parse("2026-04-16T20:00:00+09:00").toInstant(), KST);
        petalPendingReminderService = new PetalPendingReminderService(
                petalPendingReminderTargetService,
                tsunTsunRepository,
                userDeviceTokenService,
                notificationHistoryService,
                pushNotificationService,
                clock
        );
    }

    @Test
    void sendPendingReminders_sendsWhenPendingPetalExists() {
        User receiver = user(2L, true);
        LocalDate today = LocalDate.now(clock);
        given(petalPendingReminderTargetService.findCandidateUsers()).willReturn(List.of(receiver));
        given(tsunTsunRepository.countByReceiverIdAndStatus(2L, TsunTsunStatus.SENT)).willReturn(1L);
        given(userDeviceTokenService.hasActiveToken(2L)).willReturn(true);
        given(notificationHistoryService.wasSentOnDate(2L, PushNotificationType.PETAL_PENDING_REMINDER, today)).willReturn(false);
        given(notificationHistoryService.recordSent(2L, PushNotificationType.PETAL_PENDING_REMINDER, today)).willReturn(true);

        petalPendingReminderService.sendPendingReminders();

        verify(pushNotificationService).notifyPetalPendingReminder(receiver);
        verify(notificationHistoryService).recordSent(2L, PushNotificationType.PETAL_PENDING_REMINDER, today);
    }

    @Test
    void sendPendingReminders_excludesUsersWithoutPendingPetal() {
        User receiver = user(2L, true);
        given(petalPendingReminderTargetService.findCandidateUsers()).willReturn(List.of(receiver));
        given(tsunTsunRepository.countByReceiverIdAndStatus(2L, TsunTsunStatus.SENT)).willReturn(0L);

        petalPendingReminderService.sendPendingReminders();

        verify(pushNotificationService, never()).notifyPetalPendingReminder(receiver);
    }

    @Test
    void sendPendingReminders_excludesUsersWithNotificationsDisabled() {
        User receiver = user(2L, false);
        given(petalPendingReminderTargetService.findCandidateUsers()).willReturn(List.of(receiver));
        given(tsunTsunRepository.countByReceiverIdAndStatus(2L, TsunTsunStatus.SENT)).willReturn(1L);

        petalPendingReminderService.sendPendingReminders();

        verify(pushNotificationService, never()).notifyPetalPendingReminder(receiver);
    }

    @Test
    void sendPendingReminders_excludesUsersWithoutDeviceToken() {
        User receiver = user(2L, true);
        given(petalPendingReminderTargetService.findCandidateUsers()).willReturn(List.of(receiver));
        given(tsunTsunRepository.countByReceiverIdAndStatus(2L, TsunTsunStatus.SENT)).willReturn(1L);
        given(userDeviceTokenService.hasActiveToken(2L)).willReturn(false);

        petalPendingReminderService.sendPendingReminders();

        verify(pushNotificationService, never()).notifyPetalPendingReminder(receiver);
        verify(notificationHistoryService, never()).recordSent(2L, PushNotificationType.PETAL_PENDING_REMINDER, LocalDate.now(clock));
    }

    @Test
    void sendPendingReminders_preventsDuplicateSendOnSameDate() {
        User receiver = user(2L, true);
        LocalDate today = LocalDate.now(clock);
        given(petalPendingReminderTargetService.findCandidateUsers()).willReturn(List.of(receiver));
        given(tsunTsunRepository.countByReceiverIdAndStatus(2L, TsunTsunStatus.SENT)).willReturn(1L);
        given(userDeviceTokenService.hasActiveToken(2L)).willReturn(true);
        given(notificationHistoryService.wasSentOnDate(2L, PushNotificationType.PETAL_PENDING_REMINDER, today)).willReturn(true);

        petalPendingReminderService.sendPendingReminders();

        verify(pushNotificationService, never()).notifyPetalPendingReminder(receiver);
        verify(notificationHistoryService, never()).recordSent(2L, PushNotificationType.PETAL_PENDING_REMINDER, today);
    }

    @Test
    void sendPendingReminders_recordsOnlyOnceWhenHistoryInsertCollides() {
        User receiver = user(2L, true);
        LocalDate today = LocalDate.now(clock);
        given(petalPendingReminderTargetService.findCandidateUsers()).willReturn(List.of(receiver));
        given(tsunTsunRepository.countByReceiverIdAndStatus(2L, TsunTsunStatus.SENT)).willReturn(1L);
        given(userDeviceTokenService.hasActiveToken(2L)).willReturn(true);
        given(notificationHistoryService.wasSentOnDate(2L, PushNotificationType.PETAL_PENDING_REMINDER, today)).willReturn(false);
        given(notificationHistoryService.recordSent(2L, PushNotificationType.PETAL_PENDING_REMINDER, today)).willReturn(false);

        petalPendingReminderService.sendPendingReminders();

        verify(pushNotificationService).notifyPetalPendingReminder(receiver);
        verify(notificationHistoryService).recordSent(2L, PushNotificationType.PETAL_PENDING_REMINDER, today);
    }

    private User user(Long userId, boolean petalNotificationsEnabled) {
        return new User(userId, "user-" + userId, WordLevel.N4, "CODE" + userId, null, null, null, false, petalNotificationsEnabled);
    }
}
