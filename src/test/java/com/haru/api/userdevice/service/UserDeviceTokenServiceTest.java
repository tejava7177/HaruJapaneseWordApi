package com.haru.api.userdevice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.userdevice.domain.DevicePlatform;
import com.haru.api.userdevice.domain.UserDeviceToken;
import com.haru.api.userdevice.dto.RegisterDeviceTokenRequest;
import com.haru.api.userdevice.dto.RegisterDeviceTokenResponse;
import com.haru.api.userdevice.dto.UnregisterDeviceTokenResponse;
import com.haru.api.userdevice.repository.UserDeviceTokenRepository;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserDeviceTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private UserDeviceTokenService userDeviceTokenService;

    @BeforeEach
    void setUp() {
        userDeviceTokenService = new UserDeviceTokenService(userRepository, userDeviceTokenRepository);
    }

    @Test
    void registerToken_createsNewDeviceToken() {
        User user = new User(12L, "심주흔", WordLevel.N3, "7H2KQ9MP");
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest("token-1", DevicePlatform.IOS, true);
        UserDeviceToken savedToken = UserDeviceToken.create(user, "token-1", DevicePlatform.IOS, true);

        given(userRepository.findById(12L)).willReturn(Optional.of(user));
        given(userDeviceTokenRepository.findByDeviceToken("token-1")).willReturn(Optional.empty());
        given(userDeviceTokenRepository.save(any(UserDeviceToken.class))).willReturn(savedToken);

        RegisterDeviceTokenResponse response = userDeviceTokenService.registerToken(12L, request);

        assertThat(response.userId()).isEqualTo(12L);
        assertThat(response.deviceToken()).isEqualTo("token-1");
        assertThat(response.platform()).isEqualTo(DevicePlatform.IOS);
        assertThat(response.pushEnabled()).isTrue();
        assertThat(response.registered()).isTrue();
    }

    @Test
    void registerToken_updatesExistingTokenOwnerAndState() {
        User oldUser = new User(1L, "old", WordLevel.N4, "OLD00001");
        User newUser = new User(12L, "심주흔", WordLevel.N3, "7H2KQ9MP");
        UserDeviceToken existingToken = UserDeviceToken.create(oldUser, "token-1", DevicePlatform.IOS, false);
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest("token-1", DevicePlatform.IOS, true);

        given(userRepository.findById(12L)).willReturn(Optional.of(newUser));
        given(userDeviceTokenRepository.findByDeviceToken("token-1")).willReturn(Optional.of(existingToken));
        given(userDeviceTokenRepository.save(existingToken)).willReturn(existingToken);

        RegisterDeviceTokenResponse response = userDeviceTokenService.registerToken(12L, request);

        assertThat(response.userId()).isEqualTo(12L);
        assertThat(response.pushEnabled()).isTrue();
    }

    @Test
    void unregisterToken_disablesExistingToken() {
        User user = new User(12L, "심주흔", WordLevel.N3, "7H2KQ9MP");
        UserDeviceToken existingToken = UserDeviceToken.create(user, "token-1", DevicePlatform.IOS, true);

        given(userRepository.existsById(12L)).willReturn(true);
        given(userDeviceTokenRepository.findByUserIdAndDeviceToken(12L, "token-1")).willReturn(Optional.of(existingToken));

        UnregisterDeviceTokenResponse response = userDeviceTokenService.unregisterToken(12L, "token-1");

        assertThat(response.userId()).isEqualTo(12L);
        assertThat(response.deviceToken()).isEqualTo("token-1");
        assertThat(response.pushEnabled()).isFalse();
        assertThat(response.unregistered()).isTrue();
    }

    @Test
    void findActiveTokensByUserId_returnsOnlyActiveTokens() {
        User user = new User(12L, "심주흔", WordLevel.N3, "7H2KQ9MP");
        UserDeviceToken firstToken = UserDeviceToken.create(user, "token-1", DevicePlatform.IOS, true);
        UserDeviceToken secondToken = UserDeviceToken.create(user, "token-2", DevicePlatform.IOS, true);

        given(userDeviceTokenRepository.findByUserIdAndPushEnabledTrue(12L)).willReturn(List.of(firstToken, secondToken));

        List<String> tokens = userDeviceTokenService.findActiveTokensByUserId(12L);

        assertThat(tokens).containsExactly("token-1", "token-2");
    }

    @Test
    void registerToken_failsWhenUserMissing() {
        given(userRepository.findById(12L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userDeviceTokenService.registerToken(12L, new RegisterDeviceTokenRequest("token", DevicePlatform.IOS, true)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 12");
    }
}
