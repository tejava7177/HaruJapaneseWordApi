package com.haru.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import com.haru.api.user.domain.User;
import com.haru.api.user.dto.UpdateLearningLevelResponse;
import com.haru.api.user.dto.UpdatePetalNotificationsResponse;
import com.haru.api.user.dto.UpdateProfileImageResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileImageStorageService profileImageStorageService;

    @Mock
    private ActivityTrackingService activityTrackingService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, profileImageStorageService, activityTrackingService);
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
    void updateUserProfile_updatesNicknameBioAndInstagramId() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", null, null, null, true);
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.updateUserProfile(
                2L,
                "  심주흔  ",
                "  매일 한 문장씩 일본어 연습 중  ",
                "  @minsung_jp  "
        );

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.nickname()).isEqualTo("심주흔");
        assertThat(response.bio()).isEqualTo("매일 한 문장씩 일본어 연습 중");
        assertThat(response.instagramId()).isEqualTo("@minsung_jp");
        assertThat(user.getNickname()).isEqualTo("심주흔");
        assertThat(user.getBio()).isEqualTo("매일 한 문장씩 일본어 연습 중");
        assertThat(user.getInstagramId()).isEqualTo("@minsung_jp");
    }

    @Test
    void updateUserProfile_storesNullForBlankBioAndInstagramId() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", null, "@old", "old bio", true);
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.updateUserProfile(2L, null, "   ", "");

        assertThat(response.nickname()).isEqualTo("김민성");
        assertThat(response.bio()).isNull();
        assertThat(response.instagramId()).isNull();
        assertThat(user.getNickname()).isEqualTo("김민성");
        assertThat(user.getBio()).isNull();
        assertThat(user.getInstagramId()).isNull();
    }

    @Test
    void updateUserProfile_failsWhenNicknameBlank() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N");
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUserProfile(2L, "   ", "bio", "@id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nickname must not be blank");
    }

    @Test
    void updateUserProfile_failsWhenUserMissing() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserProfile(999L, "심주흔", "bio", "@id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 999");
    }

    @Test
    void getUserProfile_returnsUserProfile() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", "https://cdn.haru.app/profiles/2.png", "@minsung_jp", "매일 한 문장씩 일본어 연습 중", true);
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(2L);

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.nickname()).isEqualTo("김민성");
        assertThat(response.learningLevel()).isEqualTo(WordLevel.N2);
        assertThat(response.bio()).isEqualTo("매일 한 문장씩 일본어 연습 중");
        assertThat(response.instagramId()).isEqualTo("@minsung_jp");
        assertThat(response.buddyCode()).isEqualTo("8TR4XK6N");
        assertThat(response.randomMatchingEnabled()).isTrue();
        assertThat(response.petalNotificationsEnabled()).isTrue();
        assertThat(response.profileImageUrl()).isEqualTo("https://cdn.haru.app/profiles/2.png");
    }

    @Test
    void getUserProfile_returnsNullProfileImageUrlWhenProfileImageMissing() {
        User user = new User(3L, "하루", WordLevel.N3, "HARU0003", null, "@haru_jp", "소개", false);
        given(userRepository.findById(3L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(3L);

        assertThat(response.userId()).isEqualTo(3L);
        assertThat(response.petalNotificationsEnabled()).isTrue();
        assertThat(response.profileImageUrl()).isNull();
    }

    @Test
    void updatePetalNotifications_updatesUserSetting() {
        User user = new User(4L, "buddy4", WordLevel.N4, "BUDDY004", null, null, null, false, true);
        given(userRepository.findById(4L)).willReturn(Optional.of(user));

        UpdatePetalNotificationsResponse response = userService.updatePetalNotificationsEnabled(4L, false);

        assertThat(response.userId()).isEqualTo(4L);
        assertThat(response.petalNotificationsEnabled()).isFalse();
        assertThat(user.isPetalNotificationsEnabled()).isFalse();
    }

    @Test
    void uploadProfileImage_updatesStoredProfileImageUrl() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", null, "@minsung_jp", "매일 한 문장씩 일본어 연습 중", true);
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image".getBytes());
        given(profileImageStorageService.storeProfileImage(2L, file))
                .willReturn("/uploads/profile/2-1234.png");

        UpdateProfileImageResponse response =
                userService.uploadProfileImage(2L, file);

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.profileImageUrl()).isEqualTo("/uploads/profile/2-1234.png");
        assertThat(user.getProfileImageUrl()).isEqualTo("/uploads/profile/2-1234.png");
    }

    @Test
    void uploadProfileImage_failsWhenUserMissing() {
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image".getBytes());
        given(userRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.uploadProfileImage(2L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 2");
    }

    @Test
    void uploadProfileImage_propagatesStorageFailure() {
        User user = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", "https://cdn.haru.app/profiles/2.png", "@minsung_jp", "매일 한 문장씩 일본어 연습 중", true);
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image".getBytes());
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(profileImageStorageService.storeProfileImage(2L, file))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store profile image"));

        assertThatThrownBy(() -> userService.uploadProfileImage(2L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Failed to store profile image");
    }

    @Test
    void getUserProfile_failsWhenUserMissing() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found: 999");
    }
}
