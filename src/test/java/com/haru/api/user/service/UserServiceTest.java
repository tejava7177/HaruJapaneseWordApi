package com.haru.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.haru.api.user.domain.User;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UserBuddyCodeResponse;
import com.haru.api.user.dto.UserProfileResponse;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void updateLearningLevel_updatesUserLevel() {
        User user = new User(4L, "buddy4", WordLevel.N4, "BUDDY004");
        given(userRepository.findById(4L)).willReturn(Optional.of(user));

        UpdateLearningLevelResponse response = userService.updateLearningLevel(4L, WordLevel.N2);

        assertThat(response.userId()).isEqualTo(4L);
        assertThat(response.nickname()).isEqualTo("buddy4");
        assertThat(response.learningLevel()).isEqualTo(WordLevel.N2);
        assertThat(response.message()).contains("newly generated daily words");
        assertThat(user.getLearningLevel()).isEqualTo(WordLevel.N2);
    }

    @Test
    void updateLearningLevel_failsWhenUserMissing() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateLearningLevel(99L, WordLevel.N2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 99");
    }

    @Test
    void getBuddyCode_returnsExistingBuddyCode() {
        User user = new User(1L, "juheun", WordLevel.N4, "JUHEUN01");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserBuddyCodeResponse response = userService.getBuddyCode(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.buddyCode()).isEqualTo("JUHEUN01");
    }

    @Test
    void getBuddyCode_failsWhenUserMissing() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getBuddyCode(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 999");
    }

    @Test
    void getUserProfile_returnsUserProfile() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", null, "@minsung_jp", "매일 한 문장씩 일본어 연습 중", true);
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(2L);

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.nickname()).isEqualTo("김민성");
        assertThat(response.learningLevel()).isEqualTo(WordLevel.N2);
        assertThat(response.bio()).isEqualTo("매일 한 문장씩 일본어 연습 중");
        assertThat(response.instagramId()).isEqualTo("@minsung_jp");
        assertThat(response.buddyCode()).isEqualTo("8TR4XK6N");
        assertThat(response.randomMatchingEnabled()).isTrue();
    }

    @Test
    void getUserProfile_failsWhenUserMissing() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 999");
    }
}
