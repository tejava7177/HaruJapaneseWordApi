package com.haru.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.haru.api.auth.dto.AppleAuthRequest;
import com.haru.api.auth.dto.AppleAuthResponse;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.user.service.BuddyCodeService;
import com.haru.api.word.domain.WordLevel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AppleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BuddyCodeService buddyCodeService;

    @Mock
    private AppleIdentityTokenParser appleIdentityTokenParser;

    private AppleAuthService appleAuthService;

    @BeforeEach
    void setUp() {
        appleAuthService = new AppleAuthService(userRepository, buddyCodeService, appleIdentityTokenParser);
    }

    @Test
    void authenticate_returnsExistingUserWhenAppleSubjectExists() {
        AppleAuthRequest request = new AppleAuthRequest("token", "apple-subject-1", "updated@apple.com", "심주흔");
        User existingUser = new User(
                12L,
                "기존닉네임",
                WordLevel.N3,
                "ABCD1234",
                null,
                null,
                null,
                false,
                "apple-subject-1",
                "before@apple.com",
                "이전 이름",
                null
        );

        given(appleIdentityTokenParser.parse("token"))
                .willReturn(new AppleIdentityTokenPayload("apple-subject-1", "token@apple.com"));
        given(userRepository.findByAppleSubject("apple-subject-1")).willReturn(Optional.of(existingUser));

        AppleAuthResponse response = appleAuthService.authenticate(request);

        assertThat(response.userId()).isEqualTo(12L);
        assertThat(response.appleUserId()).isEqualTo("apple-subject-1");
        assertThat(response.nickname()).isEqualTo("기존닉네임");
        assertThat(response.email()).isEqualTo("updated@apple.com");
        assertThat(response.displayName()).isEqualTo("심주흔");
        assertThat(response.isNewUser()).isFalse();
        assertThat(existingUser.getLastLoginAt()).isNotNull();
    }

    @Test
    void authenticate_createsNewUserUsingDisplayNameWhenAppleSubjectIsNew() {
        AppleAuthRequest request = new AppleAuthRequest("token", "apple-subject-2", null, "심주흔");
        given(appleIdentityTokenParser.parse("token"))
                .willReturn(new AppleIdentityTokenPayload("apple-subject-2", "juheun9912@naver.com"));
        given(userRepository.findByAppleSubject("apple-subject-2")).willReturn(Optional.empty());
        given(userRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(new User(11L, "old", WordLevel.N4, "OLD00011")));
        given(buddyCodeService.generateUniqueBuddyCode()).willReturn("7H2KQ9MP");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        AppleAuthResponse response = appleAuthService.authenticate(request);

        assertThat(response.userId()).isEqualTo(12L);
        assertThat(response.appleUserId()).isEqualTo("apple-subject-2");
        assertThat(response.nickname()).isEqualTo("심주흔");
        assertThat(response.learningLevel()).isEqualTo(WordLevel.N5);
        assertThat(response.email()).isEqualTo("juheun9912@naver.com");
        assertThat(response.displayName()).isEqualTo("심주흔");
        assertThat(response.isNewUser()).isTrue();
    }

    @Test
    void authenticate_createsFallbackNicknameWhenDisplayNameMissing() {
        AppleAuthRequest request = new AppleAuthRequest(null, "apple-subject-3", null, null);
        given(userRepository.findByAppleSubject("apple-subject-3")).willReturn(Optional.empty());
        given(userRepository.findTopByOrderByIdDesc()).willReturn(Optional.empty());
        given(buddyCodeService.generateUniqueBuddyCode()).willReturn("5NC7PW2H");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        AppleAuthResponse response = appleAuthService.authenticate(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("사용자1");
        assertThat(response.isNewUser()).isTrue();
    }

    @Test
    void authenticate_failsWhenIdentityTokenSubjectDoesNotMatchAppleUserId() {
        AppleAuthRequest request = new AppleAuthRequest("token", "apple-subject-1", null, null);
        given(appleIdentityTokenParser.parse("token"))
                .willReturn(new AppleIdentityTokenPayload("other-subject", "token@apple.com"));

        assertThatThrownBy(() -> appleAuthService.authenticate(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("identityToken subject does not match appleUserId");
    }

    @Test
    void authenticate_returnsExistingUserWhenDuplicateAppleSubjectRaceOccurs() {
        AppleAuthRequest request = new AppleAuthRequest(null, "apple-subject-4", "after@apple.com", "심주흔");
        User existingUser = new User(
                21L,
                "심주흔",
                WordLevel.N5,
                "BUDDY021",
                null,
                null,
                null,
                false,
                "apple-subject-4",
                "after@apple.com",
                "심주흔",
                null
        );

        given(userRepository.findByAppleSubject("apple-subject-4")).willReturn(Optional.empty(), Optional.of(existingUser));
        given(userRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(new User(20L, "old", WordLevel.N4, "OLD00020")));
        given(buddyCodeService.generateUniqueBuddyCode()).willReturn("QWER1234");
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("duplicate"));

        AppleAuthResponse response = appleAuthService.authenticate(request);

        assertThat(response.userId()).isEqualTo(21L);
        assertThat(response.isNewUser()).isFalse();
    }
}
