package com.haru.api.push;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetalReminderSchedulerTest {

    @Mock
    private PetalPendingReminderService petalPendingReminderService;

    @InjectMocks
    private PetalReminderScheduler petalReminderScheduler;

    @Test
    void sendPetalPendingReminders_callsReminderService() {
        petalReminderScheduler.sendPetalPendingReminders();

        verify(petalPendingReminderService).sendPendingReminders();
    }
}
