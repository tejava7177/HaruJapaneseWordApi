package com.haru.api.push;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.userdevice.service.UserDeviceTokenService;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDeviceTokenService userDeviceTokenService;

    @Mock
    private PushSender pushSender;

    @Test
    void notifyTsunTsunReceived_sendsOnlyWhenNotificationsEnabledAndTokenExists() {
        PushNotificationService service = new PushNotificationService(userRepository, userDeviceTokenService, pushSender);
        User receiver = new User(2L, "receiver", WordLevel.N4, "BBBB2222", null, null, null, false, true);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(userDeviceTokenService.findActiveTokensByUserId(2L)).willReturn(List.of("token-1"));

        service.notifyTsunTsunReceived(2L, 88L, 1L, "sender");

        verify(pushSender).send(
                List.of("token-1"),
                "꽃잎이 도착했어요",
                "sender님이 꽃잎을 날렸어요",
                Map.of("type", "PETAL_RECEIVED", "tsunTsunId", "88", "senderUserId", "1")
        );
    }

    @Test
    void notifyTsunTsunReceived_skipsWhenTargetIsSender() {
        PushNotificationService service = new PushNotificationService(userRepository, userDeviceTokenService, pushSender);

        service.notifyTsunTsunReceived(2L, 88L, 2L, "sender");

        verify(userRepository, never()).findById(2L);
        verify(pushSender, never()).send(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void notifyTsunTsunReceived_skipsWhenNotificationsDisabled() {
        PushNotificationService service = new PushNotificationService(userRepository, userDeviceTokenService, pushSender);
        User receiver = new User(2L, "receiver", WordLevel.N4, "BBBB2222", null, null, null, false, false);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));

        service.notifyTsunTsunReceived(2L, 88L, 1L, "sender");

        verify(userDeviceTokenService, never()).findActiveTokensByUserId(2L);
        verify(pushSender, never()).send(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void notifyTsunTsunReceived_skipsWhenNoDeviceTokenExists() {
        PushNotificationService service = new PushNotificationService(userRepository, userDeviceTokenService, pushSender);
        User receiver = new User(2L, "receiver", WordLevel.N4, "BBBB2222", null, null, null, false, true);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(userDeviceTokenService.findActiveTokensByUserId(2L)).willReturn(List.of());
        given(userDeviceTokenService.hasAnyToken(2L)).willReturn(false);
        given(userDeviceTokenService.hasActiveToken(2L)).willReturn(false);

        service.notifyTsunTsunReceived(2L, 88L, 1L, "sender");

        verify(pushSender, never()).send(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void notifyTsunTsunReceived_skipsWhenAllTokensInactive() {
        PushNotificationService service = new PushNotificationService(userRepository, userDeviceTokenService, pushSender);
        User receiver = new User(2L, "receiver", WordLevel.N4, "BBBB2222", null, null, null, false, true);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(userDeviceTokenService.findActiveTokensByUserId(2L)).willReturn(List.of());
        given(userDeviceTokenService.hasAnyToken(2L)).willReturn(true);
        given(userDeviceTokenService.hasActiveToken(2L)).willReturn(false);

        service.notifyTsunTsunReceived(2L, 88L, 1L, "sender");

        verify(pushSender, never()).send(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void notifyTsunTsunReceived_logsFailureWhenPushSenderThrows() {
        PushNotificationService service = new PushNotificationService(userRepository, userDeviceTokenService, pushSender);
        User receiver = new User(2L, "receiver", WordLevel.N4, "BBBB2222", null, null, null, false, true);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(userDeviceTokenService.findActiveTokensByUserId(2L)).willReturn(List.of("token-1"));
        doThrow(new RuntimeException("boom")).when(pushSender).send(
                List.of("token-1"),
                "꽃잎이 도착했어요",
                "sender님이 꽃잎을 날렸어요",
                Map.of("type", "PETAL_RECEIVED", "tsunTsunId", "88", "senderUserId", "1")
        );

        service.notifyTsunTsunReceived(2L, 88L, 1L, "sender");

        verify(pushSender).send(
                List.of("token-1"),
                "꽃잎이 도착했어요",
                "sender님이 꽃잎을 날렸어요",
                Map.of("type", "PETAL_RECEIVED", "tsunTsunId", "88", "senderUserId", "1")
        );
    }
}
