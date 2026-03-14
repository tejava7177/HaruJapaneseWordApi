package com.haru.api.buddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.haru.api.buddy.dto.DevBuddyResetResponse;
import com.haru.api.buddy.repository.BuddyRequestRepository;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.user.init.UserDataInitializer;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuddyDevelopmentServiceTest {

    @Mock
    private TsunTsunAnswerRepository tsunTsunAnswerRepository;

    @Mock
    private TsunTsunRepository tsunTsunRepository;

    @Mock
    private BuddyRequestRepository buddyRequestRepository;

    @Mock
    private BuddyRepository buddyRepository;

    @Mock
    private BuddyRelationshipRepository buddyRelationshipRepository;

    @Mock
    private DailyWordItemRepository dailyWordItemRepository;

    @Mock
    private DailyWordSetRepository dailyWordSetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDataInitializer userDataInitializer;

    private BuddyDevelopmentService buddyDevelopmentService;

    @BeforeEach
    void setUp() {
        buddyDevelopmentService = new BuddyDevelopmentService(
                tsunTsunAnswerRepository,
                tsunTsunRepository,
                buddyRequestRepository,
                buddyRepository,
                buddyRelationshipRepository,
                dailyWordItemRepository,
                dailyWordSetRepository,
                userRepository,
                userDataInitializer
        );
    }

    @Test
    void resetBuddyData_deletesDevelopmentDataAndSeedsUsers() {
        User user1 = new User(1L, "심주흔", WordLevel.N3, "7H2KQ9MP", null, "@simjuheun", "JLPT N3 같이 준비해요", true);
        User user2 = new User(2L, "김민성", WordLevel.N2, "8TR4XK6N", null, "@minsung_jp", "매일 한 문장씩 일본어 연습 중", true);

        given(tsunTsunAnswerRepository.count()).willReturn(4L);
        given(tsunTsunRepository.count()).willReturn(3L);
        given(buddyRequestRepository.count()).willReturn(2L);
        given(buddyRepository.count()).willReturn(6L);
        given(buddyRelationshipRepository.count()).willReturn(3L);
        given(dailyWordItemRepository.count()).willReturn(8L);
        given(dailyWordSetRepository.count()).willReturn(4L);
        given(userDataInitializer.seedUsers()).willReturn(List.of(user1, user2));

        DevBuddyResetResponse response = buddyDevelopmentService.resetBuddyData();

        verify(tsunTsunAnswerRepository).deleteAllInBatch();
        verify(tsunTsunRepository).deleteAllInBatch();
        verify(buddyRequestRepository).deleteAllInBatch();
        verify(buddyRepository).deleteAllInBatch();
        verify(buddyRelationshipRepository).deleteAllInBatch();
        verify(dailyWordItemRepository).deleteAllInBatch();
        verify(dailyWordSetRepository).deleteAllInBatch();
        verify(userRepository).deleteAllInBatch();
        assertThat(response.usersReset()).isEqualTo(2);
        assertThat(response.buddyRowsDeleted()).isEqualTo(6L);
        assertThat(response.buddyRequestsDeleted()).isEqualTo(2L);
        assertThat(response.tsunTsunDeleted()).isEqualTo(3L);
        assertThat(response.seededUsers()).hasSize(2);
        assertThat(response.seededUsers().get(0).nickname()).isEqualTo("심주흔");
    }
}
