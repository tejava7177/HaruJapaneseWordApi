package com.haru.api.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PetalReminderScheduler {

    private final PetalPendingReminderService petalPendingReminderService;

    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Seoul")
    public void sendPetalPendingReminders() {
        log.info("[Push] petal pending reminder scheduler start");
        petalPendingReminderService.sendPendingReminders();
        log.info("[Push] petal pending reminder scheduler end");
    }
}
